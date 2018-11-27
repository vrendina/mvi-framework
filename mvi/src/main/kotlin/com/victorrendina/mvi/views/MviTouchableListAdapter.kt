package com.victorrendina.mvi.views

import android.arch.lifecycle.LifecycleOwner
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.victorrendina.mvi.extensions.moveItem
import com.victorrendina.mvi.extensions.removeItem

abstract class MviTouchableListAdapter<T>(lifecycleOwner: LifecycleOwner) :
    MviListAdapter<T>(lifecycleOwner) {

    protected open val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
    protected open val swipeDismissFlags = ItemTouchHelper.START or ItemTouchHelper.END

    protected open val touchHelperCallback by lazy(mode = LazyThreadSafetyMode.NONE) {
        MviItemTouchHelperCallback(this, dragFlags, swipeDismissFlags)
    }

    private val touchHelper by lazy(mode = LazyThreadSafetyMode.NONE) {
        ItemTouchHelper(touchHelperCallback)
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
        onItemMoved(fromIndex, toIndex)
    }

    fun removeItem(index: Int) {
        updateDataImmediate(data.removeItem(index), false)
        notifyItemRemoved(index)
        onItemRemoved(index)
    }

    /**
     * Override to update the view model whenever an item in the list is moved.
     */
    open fun onItemMoved(fromIndex: Int, toIndex: Int) {
    }

    /**
     * Override to update the view model whenever an item in the list is swiped to dismiss.
     */
    open fun onItemRemoved(index: Int) {
    }
}