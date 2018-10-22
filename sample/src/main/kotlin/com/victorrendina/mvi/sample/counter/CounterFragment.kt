package com.victorrendina.mvi.sample.counter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.victorrendina.mvi.extensions.activityViewModel
import com.victorrendina.mvi.extensions.fragmentViewModel
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import kotlinx.android.synthetic.main.fragment_counter.*

class CounterFragment : BaseFragment() {

    private val viewModel: CounterViewModel by fragmentViewModel()
    private val sharedViewModel: CounterViewModel by activityViewModel()

    /**
     * Subscriptions to the view model need to be done in the [onCreate] method so they aren't created multiple times
     * if the fragment is put into the back stack. It is safe to access views from blocks inside the subscribers because
     * the state updates are only delivered when the view in the started state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Subscribe to fragment view model count updates
        viewModel.selectSubscribe(CounterViewState::count) {
            coloredCounter.text = it.toString()
        }

        // Subscribe to activity view model count updates
        sharedViewModel.selectSubscribe(CounterViewState::count) {
            sharedCounter.text = it.toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_counter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        increaseFragment.setOnClickListener { viewModel.increaseCount() }
        decreaseFragment.setOnClickListener { viewModel.decreaseCount() }
        increaseShared.setOnClickListener { sharedViewModel.increaseCount() }
        decreaseShared.setOnClickListener { sharedViewModel.decreaseCount() }
    }
}