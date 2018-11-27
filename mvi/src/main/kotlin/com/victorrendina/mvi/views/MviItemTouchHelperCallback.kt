package com.victorrendina.mvi.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper

open class MviItemTouchHelperCallback(
    private val adapter: MviTouchableListAdapter<*>,
    private val dragFlags: Int,
    private val swipeDismissFlags: Int
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        viewHolder as MviListViewHolder<*>

        val dragFlags = if (viewHolder.moveEnabled) this.dragFlags else 0
        val swipeDismissFlags = if (viewHolder.swipeDismissEnabled) this.swipeDismissFlags else 0

        return makeMovementFlags(dragFlags, swipeDismissFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        source: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        target as MviListViewHolder<*>

        if (!target.moveEnabled) return false

        adapter.moveItem(source.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.removeItem(viewHolder.adapterPosition)
    }
}