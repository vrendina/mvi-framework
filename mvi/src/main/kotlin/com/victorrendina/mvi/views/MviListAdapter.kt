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
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

abstract class MviListAdapter<T, H : MviListViewHolder<T>>(lifecycleOwner: LifecycleOwner) :
    RecyclerView.Adapter<H>() {

    protected var data: List<T> = emptyList()
        private set

    private val subject: Subject<MviDiffRequest> = PublishSubject.create()
    private val subscription: Disposable

    private val viewHolders = HashSet<MviListViewHolder<T>>(10)

    init {
        subscription = subject.switchMap { calculateDiff(it) }
            .observeOn(AndroidSchedulers.mainThread()).subscribe {
                if (it.oldList === data) {
                    data = it.newList
                    it.result.dispatchUpdatesTo(this)
                }
            }

        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                lifecycleOwner.lifecycle.removeObserver(this)
                subscription.dispose()
                destroyViewHolders()
            }
        })
    }

    @CallSuper
    override fun onBindViewHolder(holder: H, position: Int) {
        holder.bind(data[position])
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
    override fun onViewAttachedToWindow(holder: H) {
        super.onViewAttachedToWindow(holder)
        holder.attach()
    }

    @CallSuper
    override fun onViewDetachedFromWindow(holder: H) {
        super.onViewDetachedFromWindow(holder)
        holder.detach()
    }

    @CallSuper
    override fun onViewRecycled(holder: H) {
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
        subject.onNext(MviDiffRequest(this.data, data))
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
        }.subscribeOn(Schedulers.computation())
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
        val result: DiffUtil.DiffResult = DiffUtil.calculateDiff(diffCallback(oldList, newList))
    }
}