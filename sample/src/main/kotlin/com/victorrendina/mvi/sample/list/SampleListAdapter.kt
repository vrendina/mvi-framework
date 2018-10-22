package com.victorrendina.mvi.sample.list

import android.arch.lifecycle.LifecycleOwner
import android.view.LayoutInflater
import android.view.ViewGroup
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.views.MviTouchableListAdapter

class SampleListAdapter(private val lifecycleOwner: LifecycleOwner, private val viewModel: SampleListViewModel) :
    MviTouchableListAdapter<EntityListItem, SampleListViewHolder>(lifecycleOwner) {

    override val dragEnabled = true
    override val swipeDismissEnabled = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SampleListViewHolder {
        val containerView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_entity, parent, false)
        return SampleListViewHolder(containerView, lifecycleOwner, viewModel)
    }

    override fun syncMoveToViewModel(fromIndex: Int, toIndex: Int) {
        viewModel.moveEntity(fromIndex, toIndex)
    }

    override fun syncSwipeDismissToViewModel(index: Int) {
        // Swipe to dismiss not enabled
    }

}