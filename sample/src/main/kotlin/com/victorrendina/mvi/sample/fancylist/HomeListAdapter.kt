package com.victorrendina.mvi.sample.fancylist

import android.arch.lifecycle.LifecycleOwner
import android.view.ViewGroup
import com.victorrendina.mvi.extensions.inflate
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.fancylist.holders.HomeFooterViewHolder
import com.victorrendina.mvi.sample.fancylist.holders.HomeHeaderViewHolder
import com.victorrendina.mvi.sample.fancylist.holders.HomeServiceViewHolder
import com.victorrendina.mvi.views.MviListViewHolder
import com.victorrendina.mvi.views.MviTouchableListAdapter

class HomeListAdapter(lifecycleOwner: LifecycleOwner, private val viewModel: HomeListViewModel) :
    MviTouchableListAdapter<HomeListItem>(lifecycleOwner) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MviListViewHolder<out HomeListItem> {
        return when (viewType) {
            VIEW_TYPE_HEADER_ITEM -> {
                HomeHeaderViewHolder(parent.inflate(R.layout.list_item_header), viewModel)
            }
            VIEW_TYPE_FOOTER_ITEM -> {
                HomeFooterViewHolder(parent.inflate(R.layout.list_item_footer), viewModel)
            }
            VIEW_TYPE_SERVICE_ITEM -> {
                HomeServiceViewHolder(parent.inflate(R.layout.list_item_service), viewModel)
            }
            else -> throw IllegalArgumentException("Unknown view holder type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is HomeHeaderItem -> VIEW_TYPE_HEADER_ITEM
            is HomeFooterItem -> VIEW_TYPE_FOOTER_ITEM
            is HomeServiceItem -> VIEW_TYPE_SERVICE_ITEM
        }
    }

    override fun onItemMoved(fromIndex: Int, toIndex: Int) {
//        viewModel.moveEntity(fromIndex, toIndex)
    }

    companion object {
        const val VIEW_TYPE_SERVICE_ITEM = 1
        const val VIEW_TYPE_HEADER_ITEM = 2
        const val VIEW_TYPE_FOOTER_ITEM = 3
    }
}