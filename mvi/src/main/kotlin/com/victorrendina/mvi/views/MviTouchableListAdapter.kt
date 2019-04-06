package com.victorrendina.mvi.views

import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.victorrendina.mvi.extensions.moveItem
import com.victorrendina.mvi.extensions.removeItem

abstract class MviTouchableListAdapter<T>(lifecycleOwner: LifecycleOwner) :
    MviListAdapter<T>(lifecycleOwner) {

    open val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
    open val swipeDismissFlags = ItemTouchHelper.START or ItemTouchHelper.END

    // If long press dragging is disabled the view holder should call touchHelper.startDrag() when dragging should start
    open val longPressDragEnabled = true

    protected open val touchHelperCallback by lazy(mode = LazyThreadSafetyMode.NONE) {
        MviItemTouchHelperCallback(this)
    }

    protected open val touchHelper by lazy(mode = LazyThreadSafetyMode.NONE) {
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
        onUserMovedItem(fromIndex, toIndex)
    }

    fun removeItem(index: Int) {
        updateDataImmediate(data.removeItem(index), false)
        notifyItemRemoved(index)
        onUserRemovedItem(index)
    }

    open fun onDragStart(viewHolder: RecyclerView.ViewHolder) {
    }

    open fun onSwipeStart(viewHolder: RecyclerView.ViewHolder) {
    }

    open fun onTouchComplete(viewHolder: RecyclerView.ViewHolder) {
    }

    /**
     * Override to update the view model whenever an item in the list is moved.
     */
    open fun onUserMovedItem(fromIndex: Int, toIndex: Int) {
    }

    /**
     * Override to update the view model whenever an item in the list is swiped to dismiss.
     */
    open fun onUserRemovedItem(index: Int) {
    }
}