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

    private var viewHolderId = 0 // create a new id for each view holder that is created for logging

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MviListViewHolder<out SampleListItem> {
        return SingleSelectionViewHolder(viewHolderId++, parent.inflate(R.layout.list_item_single_selection))
    }

    inner class SingleSelectionViewHolder(val id: Int, itemView: View) : MviListViewHolder<SampleListItem>(itemView) {

        private val disposable: Disposable

        init {
            itemView.setOnClickListener {
                boundItem?.also { item ->
                    viewModel.updateSelection(item)
                }
            }

            // Log the adapter position and what item is bound every 2 seconds
            disposable = Observable.interval(2L, TimeUnit.SECONDS).subscribe {
//                Log.d(tag, "$adapterPosition: subscription test bound item: $boundItem")
            }

            // Subscribe to be notified whenever the selected item changes, view model subscriptions don't need to be disposed
            viewModel.selectSubscribe(SingleSelectionListViewState::selectedItemId) {
                bindSelection()
            }

            Log.d(tag, "$id create new")
        }

        override fun onBind(item: SampleListItem) {
            textView.text = "${item.title} - holder $id"
            randomTextView.text = item.randomInt.toString()
            bindSelection()
            Log.d(tag, "$id onBind position $adapterPosition")
        }

        override fun onStart() {
            radioButton.jumpDrawablesToCurrentState() // Don't animate the radio button when the view is added

            // Do some animation that needs to be cancelled to recycle the view holder
            textView.rotationY = 0f
            textView.animate().rotationY(360f).setDuration(2000L).start()

            Log.d(tag, "$id onStart position $adapterPosition")
        }

        override fun onStop() {
            Log.d(tag, "$id onStop position $adapterPosition")
        }

        override fun cancelAnimations() {
            textView.animate().cancel()
            Log.d(tag, "$id cancelAnimations position $adapterPosition")
        }

        override fun onRecycle() {
            Log.d(tag, "$id onRecycle position $adapterPosition")
        }

        override fun onDestroy() {
            disposable.dispose()
            Log.d(tag, "$id onDestroy")
        }

        private fun bindSelection() {
            // Get the currently selected item from the view model synchronously
            val selectedItemId = withState(viewModel) { it.selectedItemId }
            radioButton.isChecked = selectedItemId != null && selectedItemId == boundItem?.id
        }
    }
}