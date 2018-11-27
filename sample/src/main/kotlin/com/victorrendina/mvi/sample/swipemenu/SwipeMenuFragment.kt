package com.victorrendina.mvi.sample.swipemenu

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.victorrendina.mvi.lifecycleLazy
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import kotlinx.android.synthetic.main.fragment_home_list.*

class SwipeMenuFragment : BaseFragment() {

    // Example of view model that does not need state subscriptions
    private val viewModel by lifecycleLazy {
        ViewModelProviders.of(this)[SwipeMenuViewModel::class.java]
    }

    private val adapter by lazy(mode = LazyThreadSafetyMode.NONE) {
        SwipeMenuAdapter(this, viewModel.openItems)
    }

    // Create some fake data
    private val data = ArrayList<Int>(100).apply {
        for (i in 0..100) {
            add(i)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter.updateDataImmediate(data)
        recyclerView.adapter = adapter
    }
}