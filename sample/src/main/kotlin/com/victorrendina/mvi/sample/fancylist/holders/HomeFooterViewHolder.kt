package com.victorrendina.mvi.sample.fancylist.holders

import android.view.View
import com.victorrendina.mvi.sample.fancylist.HomeListItem
import com.victorrendina.mvi.sample.fancylist.HomeListViewModel
import com.victorrendina.mvi.views.MviListViewHolder

class HomeFooterViewHolder(
    itemView: View,
    private val viewModel: HomeListViewModel
) : MviListViewHolder<HomeListItem>(itemView) {

    init {
    }

    override fun onBind(item: HomeListItem) {
    }
}