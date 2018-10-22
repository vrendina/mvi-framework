package com.victorrendina.mvi.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

internal class ItemTouchHelperCallback(
    private val listener: MviTouchableListAdapter<*, *>,
    private val dragEnabled: Boolean,
    private val swipeDismissEnabled: Boolean,
    private val dragFlags: Int,
    private val swipeDismissFlags: Int
) : ItemTouchHelper.Callback() {

    override fun isItemViewSwipeEnabled(): Boolean = swipeDismissEnabled

    override fun isLongPressDragEnabled(): Boolean = dragEnabled

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(dragFlags, swipeDismissFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        listener.moveItem(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        listener.removeItem(viewHolder.adapterPosition)
    }
}