package com.victorrendina.mvi.views

import androidx.recyclerview.widget.RecyclerView

class MviRecycledViewPool : RecyclerView.RecycledViewPool() {

    private val viewTypes = HashSet<Int>()

    override fun putRecycledView(scrap: RecyclerView.ViewHolder) {
        val count = getRecycledViewCount(scrap.itemViewType)
        super.putRecycledView(scrap)

        if (getRecycledViewCount(scrap.itemViewType) > count) {
            // Keep track of the item types in the pool
            viewTypes.add(scrap.itemViewType)
        } else {
            // If we fail to add the view holder to the pool then destroy it
            (scrap as? MviListViewHolder<*>)?.destroy()
        }
    }

    override fun setMaxRecycledViews(viewType: Int, max: Int) {
        while (getRecycledViewCount(viewType) > max) {
            (getRecycledView(viewType) as? MviListViewHolder<*>)?.destroy()
        }
        super.setMaxRecycledViews(viewType, max)
    }

    override fun clear() {
        viewTypes.forEach { viewType ->
            while (true) {
                val viewHolder = getRecycledView(viewType) ?: break
                (viewHolder as? MviListViewHolder<*>)?.destroy()
            }
        }
        super.clear()
    }

}