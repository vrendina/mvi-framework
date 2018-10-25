package com.victorrendina.mvi.sample.fancylist

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.victorrendina.mvi.extensions.fragmentViewModel
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import com.victorrendina.mvi.withState
import kotlinx.android.synthetic.main.fragment_sample_list.*

class HomeListFragment : BaseFragment() {

    private val viewModel: HomeListViewModel by fragmentViewModel()

    private val adapter by lazy(mode = LazyThreadSafetyMode.NONE) {
        HomeListAdapter(this, viewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.selectSubscribe(HomeListViewState::items) {
            adapter.updateData(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
        withState(viewModel) {
            adapter.updateDataImmediate(it.items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Adapter must be set to null in onDestroyView so resources can be released
        recyclerView.adapter = null
    }
}