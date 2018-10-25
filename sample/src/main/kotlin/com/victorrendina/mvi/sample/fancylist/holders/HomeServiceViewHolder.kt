package com.victorrendina.mvi.sample.fancylist.holders

import android.util.Log
import android.view.View
import com.victorrendina.mvi.sample.fancylist.HomeListItem
import com.victorrendina.mvi.sample.fancylist.HomeListViewModel
import com.victorrendina.mvi.sample.fancylist.HomeListViewState
import com.victorrendina.mvi.views.MviListViewHolder

class HomeServiceViewHolder(
    itemView: View,
    viewModel: HomeListViewModel
) : MviListViewHolder<HomeListItem>(itemView) {

    override val moveEnabled: Boolean = true

    init {
        viewModel.selectSubscribe(this, HomeListViewState::editMode) {
            Log.d("ViewHolder", "Received edit mode state update: $it")
        }

        itemView.setOnLongClickListener {
            Log.d("ViewHolder", "Long clicked on view $adapterPosition")
            viewModel.setEditMode(true)
            false
        }
    }

    override fun onBind(item: HomeListItem) {
    }
}