package com.victorrendina.mvi.views

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.CallSuper
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

abstract class MviListAdapter<T>(lifecycleOwner: LifecycleOwner) : RecyclerView.Adapter<MviListViewHolder<out T>>() {

    protected var data: List<T> = emptyList()
        private set

    // Holds the subscription for the active DiffUtil request
    private var subscription: Disposable? = null

    private val viewHolders = HashSet<MviListViewHolder<*>>(10)

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                lifecycleOwner.lifecycle.removeObserver(this)
                subscription?.dispose()
                destroyViewHolders()
            }
        })
    }

    @CallSuper
    override fun onBindViewHolder(holder: MviListViewHolder<out T>, position: Int) {
        @Suppress("UNCHECKED_CAST")
        (holder as MviListViewHolder<T>).bind(data[position])
        viewHolders.add(holder)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        // Change animations cause views to flash when they are updated and breaks sliders when user is interacting
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    @CallSuper
    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        destroyViewHolders()
    }

    override fun getItemCount(): Int = data.size

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

    private fun destroyViewHolders() {
        viewHolders.forEach { it.destroy() }
        viewHolders.clear()
    }

    /**
     * Update the list of data associated with this adapter. This will trigger the differ
     * to check for differences in the data set on a background thread.
     */
    fun updateData(data: List<T>) {
        subscription?.dispose()
        subscription = calculateDiff(MviDiffRequest(this.data, data))
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::updateData)
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
     * Compare two items based on their ids to determine if they are the same. If the ids
     * don't match this method should return false and it will save a step of having to look
     * at the contents of the objects. If this method returns true [areContentsTheSame] will
     * be called.
     *
     * @return true if ids match or don't exist, false if ids are different
     */
    open fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return true
    }

    open fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    private fun calculateDiff(request: MviDiffRequest): Observable<MviDiffResult> {
        return Observable.fromCallable {
            MviDiffResult(request.oldList, request.newList)
        }
    }

    private fun updateData(result: MviDiffResult) {
        if (result.oldList === data) {
            this.data = result.newList
            result.diff.dispatchUpdatesTo(this)
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
        }
    }

    inner class MviDiffRequest(val oldList: List<T>, val newList: List<T>)

    inner class MviDiffResult(val oldList: List<T>, val newList: List<T>) {
        val diff: DiffUtil.DiffResult = DiffUtil.calculateDiff(diffCallback(oldList, newList))
    }
}