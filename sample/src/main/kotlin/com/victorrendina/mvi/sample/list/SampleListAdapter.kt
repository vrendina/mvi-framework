package com.victorrendina.mvi.sample.list

import android.arch.lifecycle.LifecycleOwner
import android.view.LayoutInflater
import android.view.ViewGroup
import com.victorrendina.mvi.extensions.inflate
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.views.MviListViewHolder
import com.victorrendina.mvi.views.MviTouchableListAdapter

class SampleListAdapter(lifecycleOwner: LifecycleOwner, private val viewModel: SampleListViewModel) :
    MviTouchableListAdapter<EntityListItem>(lifecycleOwner) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MviListViewHolder<out EntityListItem> {
        return SampleListViewHolder(parent.inflate(R.layout.list_item_entity), viewModel)
    }

    override fun onItemMoved(fromIndex: Int, toIndex: Int) {
        viewModel.moveEntity(fromIndex, toIndex)
    }

    override fun onItemRemoved(index: Int) {
        viewModel.removeEntity(index)
    }
}