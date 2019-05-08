package com.victorrendina.mvi.sample.lists.single

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.victorrendina.mvi.extensions.viewModel
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import com.victorrendina.mvi.sample.framework.nav.screen
import com.victorrendina.mvi.withState
import kotlinx.android.synthetic.main.fragment_sample_list.*

class SingleSelectionListFragment : BaseFragment() {

    private val viewModel: SingleSelectionListViewModel by viewModel()

    private val adapter by lazy(mode = LazyThreadSafetyMode.NONE) {
        SingleSelectionListAdapter(this, viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.selectSubscribe(SingleSelectionListViewState::items) { items ->
            adapter.updateData(items)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sample_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = adapter
        recyclerView.setItemViewCacheSize(2)
        recyclerView.recycledViewPool.setMaxRecycledViews(0, 4)

        withState(viewModel) {
            adapter.updateDataImmediate(it.items)
        }

        pushScreenButton.setOnClickListener {
            nav.pushScreen(screen(SingleSelectionListFragment::class.java))
        }
    }
}