package com.victorrendina.mvi.views

import android.util.Log
import androidx.annotation.CallSuper
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * The [MviListAdapter] serves as the base adapter for all RecyclerView based lists. The adapter is designed
 * to be created as a field within fragments and then attached to a RecyclerView when the fragment's view has been
 * created.
 *
 * To construct an instance of the [MviListAdapter] a fragment must be provided so the lifecycle of the view holders
 * can be managed appropriately.
 *
 * ```
 * val myAdapter = MyAdapter(this)
 *
 * override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *      super.onViewCreated(view, savedInstanceState)
 *      recyclerView.adapter = adapter
 * }
 * ```
 */
abstract class MviListAdapter<T : Any>(fragment: Fragment) : RecyclerView.Adapter<MviListViewHolder<out T>>() {

    protected var data: List<T> = emptyList()
        private set(newList) {
            val oldList = field
            field = newList
            onDataUpdated(oldList, newList)
        }

    /**
     * If the DiffUtil calculations should detect items being moved in the list. If items will never
     * be moved in the list this should be set to false to save an extra pass in the calculations.
     *
     * If an item is moved when this is set to false the DiffUtil will report an item as removed and then
     * added rather than moved, so the item will still get moved it just won't have a nice animation.
     */
    protected open val detectMoves = false

    /**
     * Log diagnostic information about the results of the diff calculation.
     */
    protected open val logDiffResults = false

    protected val tag: String by lazy { javaClass.simpleName }

    // Holds the subscription for the active DiffUtil request
    private var subscription: Disposable? = null
    private var diffStartTime: Long = 0L

    private var recyclerView: RecyclerView? = null

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment, Observer<LifecycleOwner> { owner ->
            owner.lifecycle.addObserver(object : LifecycleObserver {
                @Suppress("unused")
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroyView() {
                    owner.lifecycle.removeObserver(this)
                    subscription?.dispose()

                    val recyclerView = recyclerView
                    if (recyclerView != null) {
                        for (index in 0 until data.size) {
                            val viewHolder = recyclerView.findViewHolderForAdapterPosition(index)
                            if (viewHolder != null) {
                                // Prevent the view holders that are currently on the screen from being put in the pool
                                recyclerView.layoutManager?.ignoreView(viewHolder.itemView)
                                (viewHolder as? MviListViewHolder<*>)?.destroy()
                            }
                        }

                        recyclerView.itemAnimator?.endAnimations()
                        recyclerView.adapter = null // Removing the adapter adds all the view holders to the pool
                        recyclerView.recycledViewPool.clear() // Destroy any remaining items in the pool
                    }
                }
            })
        })
    }

    @CallSuper
    override fun onBindViewHolder(holder: MviListViewHolder<out T>, position: Int) {
        @Suppress("UNCHECKED_CAST")
        (holder as MviListViewHolder<T>).bind(data[position])
    }

    @CallSuper
    override fun onBindViewHolder(holder: MviListViewHolder<out T>, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val changeSet = HashSet<String>()
            // Combine all the payloads into a single change set
            payloads.forEach {
                @Suppress("UNCHECKED_CAST")
                changeSet.addAll(it as Set<String>)
            }

            if (changeSet.isEmpty()) {
                // If there were no changes detected, call through to the normal [onBindViewHolder] method
                super.onBindViewHolder(holder, position, payloads)
            } else {
                @Suppress("UNCHECKED_CAST")
                (holder as MviListViewHolder<T>).bind(data[position], changeSet)
            }
        }
    }

    @CallSuper
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        // Change animations cause views to flash when they are updated and breaks sliders when user is interacting
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        if (recyclerView.recycledViewPool !is MviRecycledViewPool) {
            recyclerView.setRecycledViewPool(MviRecycledViewPool())
        }
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    final override fun getItemCount(): Int = data.size

    @CallSuper
    override fun onViewAttachedToWindow(holder: MviListViewHolder<out T>) {
        super.onViewAttachedToWindow(holder)
        holder.attach()
    }

    @CallSuper
    override fun onViewDetachedFromWindow(holder: MviListViewHolder<out T>) {
        super.onViewDetachedFromWindow(holder)
        holder.detach()
    }

    @CallSuper
    override fun onViewRecycled(holder: MviListViewHolder<out T>) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    final override fun onFailedToRecycleView(holder: MviListViewHolder<out T>): Boolean {
        holder.cancelAnimations()
        if (ViewCompat.hasTransientState(holder.itemView)) {
            Log.e(
                tag,
                "Failed to recycle view because animations were not cancelled. Make sure you override cancelAnimations() in your view holder and stop any running animations."
            )
            holder.setIsRecyclable(false)
            holder.destroy()
            return false
        }
        return true
    }

    /**
     * Update the list of data associated with this adapter. This will trigger the differ
     * to check for differences in the data set on a background thread. If the adapter
     * doesn't have any data then notifyItemRangeInserted will be called.
     */
    fun updateData(data: List<T>) {
        when {
            itemCount == 0 -> updateDataImmediate(data)
            data.isEmpty() -> {
                // New data set is empty, notify that all items have been removed
                val removedItemCount = itemCount
                this.data = data
                notifyItemRangeRemoved(0, removedItemCount)
            }
            else -> {
                // Run diffutil on a background thread
                diffStartTime = System.currentTimeMillis()
                subscription?.dispose()
                subscription = calculateDiff(MviDiffRequest(this.data, data))
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(::updateData)
            }
        }
    }

    /**
     * Update the data associated with this adapter immediately without triggering a diff calculation.
     * This method is expensive and will call [notifyDataSetChanged] so should only be used when the adapter
     * is created and needs to restore the previous scroll position.
     */
    fun updateDataImmediate(data: List<T>, notify: Boolean = true) {
        this.data = data
        if (notify) {
            notifyDataSetChanged()
        }
    }

    /**
     * Called when the list of data is updated but before any changes are made to the view.
     */
    open fun onDataUpdated(oldList: List<T>, newList: List<T>) {
    }

    /**
     * Compare two items based on their ids to determine if they are the same. If the ids
     * don't match this method should return false and it will save a step of having to look
     * at the contents of the objects. If this method returns true [areContentsTheSame] will
     * be called.
     *
     * @return true if ids match or don't exist, false if ids are different
     */
    open fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem::class == newItem::class
    }

    open fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    /**
     * If [areContentsTheSame] returns false this method can be overridden to return a set that
     * contains keys that indicate which data has changed. When the ViewHolder is updated the
     * bind(item, changeSet) method will be invoked and the ViewHolder should only update items indicated
     * by the changeSet.
     */
    open fun getChangeSet(oldItem: T, newItem: T): Set<String> {
        return emptySet()
    }

    private fun calculateDiff(request: MviDiffRequest): Observable<MviDiffResult> {
        return Observable.fromCallable {
            MviDiffResult(request.oldList, request.newList)
        }
    }

    private fun updateData(result: MviDiffResult) {
        if (result.oldList === data) {
            logDiffResult("Diff calculation took ${System.currentTimeMillis() - diffStartTime}ms")
            this.data = result.newList
            result.diff.dispatchUpdatesTo(object : ListUpdateCallback {
                override fun onChanged(position: Int, count: Int, payload: Any?) {
                    notifyItemRangeChanged(position, count, payload)
                    logDiffResult("onChanged - position: $position count: $count changes: $payload")
                }

                override fun onMoved(fromPosition: Int, toPosition: Int) {
                    notifyItemMoved(fromPosition, toPosition)
                    logDiffResult("onMoved - from: $fromPosition to: $toPosition")
                }

                override fun onInserted(position: Int, count: Int) {
                    notifyItemRangeInserted(position, count)
                    logDiffResult("onInserted - position: $position count: $count old list size: ${result.oldList.size} new list size: ${result.newList.size}")
                    /*
                     If items are appended to the end of the list, the previous last item needs to be updated so
                     item decorations are re-drawn correctly.
                     */
                    if (position == result.oldList.size) {
                        notifyItemChanged(position - 1, null)
                    }
                }

                override fun onRemoved(position: Int, count: Int) {
                    notifyItemRangeRemoved(position, count)
                    logDiffResult("onRemoved - position: $position count: $count")
                }
            })
        }
    }

    private fun logDiffResult(message: String) {
        if (logDiffResults) {
            Log.d(tag, message)
        }
    }

    private fun diffCallback(oldList: List<T>, newList: List<T>): DiffUtil.Callback {
        return object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
            }

            override fun getOldListSize(): Int = oldList.size

            override fun getNewListSize(): Int = newList.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                val changeSet = getChangeSet(oldList[oldItemPosition], newList[newItemPosition])
                return if (changeSet.isEmpty()) null else changeSet
            }
        }
    }

    inner class MviDiffRequest(val oldList: List<T>, val newList: List<T>)

    inner class MviDiffResult(val oldList: List<T>, val newList: List<T>) {
        val diff: DiffUtil.DiffResult = DiffUtil.calculateDiff(diffCallback(oldList, newList), detectMoves)
    }
}
