package com.victorrendina.mvi.sample.lists.single

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.victorrendina.mvi.extensions.inflate
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.lists.SampleListItem
import com.victorrendina.mvi.views.MviListAdapter
import com.victorrendina.mvi.views.MviListViewHolder
import com.victorrendina.mvi.withState
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.list_item_single_selection.*
import java.util.concurrent.TimeUnit

class SingleSelectionListAdapter(
    fragment: Fragment,
    private val viewModel: SingleSelectionListViewModel
) : MviListAdapter<SampleListItem>(fragment) {

    override val logDiffResults = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MviListViewHolder<out SampleListItem> {
        return SingleSelectionViewHolder(parent.inflate(R.layout.list_item_single_selection))
    }

    inner class SingleSelectionViewHolder(itemView: View) : MviListViewHolder<SampleListItem>(itemView) {

        private var disposable: Disposable? = null

        init {
            itemView.setOnClickListener {
                boundItem?.also { item ->
                    viewModel.updateSelection(item)
                }
            }
        }

        override fun onBind(item: SampleListItem) {
            Log.d(tag, "$adapterPosition bound")
            textView.text = item.title
            randomTextView.text = item.randomInt.toString()
            bindSelection()

            // Subscribe to be notified whenever the selected item changes, view model subscriptions don't need to be disposed
            viewModel.selectSubscribe(SingleSelectionListViewState::selectedItemId) {
                bindSelection()
            }

            // Log the adapter position and what item is bound every 2 seconds
            disposable = Observable.interval(2L, TimeUnit.SECONDS).subscribe {
                Log.d(tag, "$adapterPosition: bound item: $boundItem")
            }
        }

        private fun bindSelection() {
            // Get the currently selected item from the view model synchronously
            val selectedItemId = withState(viewModel) { it.selectedItemId }
            radioButton.isChecked = selectedItemId != null && selectedItemId == boundItem?.id
        }

        override fun onRecycle() {
            Log.d(tag, "$adapterPosition recycled")
            disposable?.dispose()
            disposable = null
        }
    }
}