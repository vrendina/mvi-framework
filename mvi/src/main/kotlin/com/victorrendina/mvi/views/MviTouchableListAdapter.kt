package com.victorrendina.mvi.views

import android.arch.lifecycle.LifecycleOwner
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.victorrendina.mvi.extensions.moveItem
import com.victorrendina.mvi.extensions.removeItem

abstract class MviTouchableListAdapter<T, H : MviListViewHolder<T>>(lifecycleOwner: LifecycleOwner) :
    MviListAdapter<T, H>(lifecycleOwner) {

    protected open val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
    protected open val swipeDismissFlags = ItemTouchHelper.START or ItemTouchHelper.END

    private val touchHelper by lazy(mode = LazyThreadSafetyMode.NONE) {
        ItemTouchHelper(
            ItemTouchHelperCallback(
                this,
                dragFlags,
                swipeDismissFlags
            )
        )
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        touchHelper.attachToRecyclerView(null)
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        updateDataImmediate(data.moveItem(fromIndex, toIndex), false)
        notifyItemMoved(fromIndex, toIndex)
        syncMoveToViewModel(fromIndex, toIndex)
    }

    fun removeItem(index: Int) {
        updateDataImmediate(data.removeItem(index), false)
        notifyItemRemoved(index)
        syncSwipeDismissToViewModel(index)
    }

    /**
     * Override to update the view model whenever an item in the list is moved.
     */
    open fun syncMoveToViewModel(fromIndex: Int, toIndex: Int) {
    }

    /**
     * Override to update the view model whenever an item in the list is swiped to dismiss.
     */
    open fun syncSwipeDismissToViewModel(index: Int) {
    }
}