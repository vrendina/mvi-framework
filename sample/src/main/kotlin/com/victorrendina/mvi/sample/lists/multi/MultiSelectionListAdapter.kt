package com.victorrendina.mvi.sample.lists.multi

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.victorrendina.mvi.extensions.inflate
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.lists.SampleListItem
import com.victorrendina.mvi.views.MviListViewHolder
import com.victorrendina.mvi.views.MviTouchableListAdapter
import com.victorrendina.mvi.views.SelectableListItem
import kotlinx.android.synthetic.main.list_item_multi_selection.*

class MultiSelectionListAdapter(
    fragment: Fragment,
    private val viewModel: MultiSelectionListViewModel
) : MviTouchableListAdapter<SelectableListItem<SampleListItem>>(fragment) {

    override val logDiffResults = true
    override val detectMoves = true

    private var viewHolderId = 0 // create a new id for each view holder that is created for logging

    override fun areItemsTheSame(
        oldItem: SelectableListItem<SampleListItem>,
        newItem: SelectableListItem<SampleListItem>
    ): Boolean {
        return oldItem.data.id == newItem.data.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MviListViewHolder<out SelectableListItem<SampleListItem>> {
        return MultiSelectionViewHolder(viewHolderId++, parent.inflate(R.layout.list_item_multi_selection))
    }

    override fun onUserMovedItem(fromIndex: Int, toIndex: Int) {
        viewModel.moveItem(fromIndex, toIndex)
    }

    override fun onUserRemovedItem(index: Int) {
        viewModel.removeItem(index)
    }

    inner class MultiSelectionViewHolder(val id: Int, itemView: View) : MviListViewHolder<SelectableListItem<SampleListItem>>(itemView) {

        override val moveEnabled = true
        override val swipeDismissEnabled = true

        init {
            itemView.setOnClickListener {
                boundItem?.also { item ->
                    viewModel.toggleSelection(item)
                }
            }
        }

        override fun onBind(item: SelectableListItem<SampleListItem>) {
            textView.text = item.data.title
            randomTextView.text = item.data.randomInt.toString()
            checkBox.isChecked = item.selected

            Log.d(tag, "$id onBind position $adapterPosition")
        }

        override fun onStart() {
            Log.d(tag, "$id onStart position $adapterPosition")
        }

        override fun onStop() {
            Log.d(tag, "$id onStop position $adapterPosition")
        }

        override fun onRecycle() {
            Log.d(tag, "$id onRecycle position $adapterPosition")
        }

        override fun onDestroy() {
            Log.d(tag, "$id onDestroy")
        }
    }
}