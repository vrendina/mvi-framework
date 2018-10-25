package com.victorrendina.mvi.sample.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.victorrendina.mvi.Incomplete
import com.victorrendina.mvi.Success
import com.victorrendina.mvi.extensions.viewModel
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import com.victorrendina.mvi.withState
import kotlinx.android.synthetic.main.fragment_sample_list.*

class SampleListFragment : BaseFragment() {

    private val viewModel: SampleListViewModel by viewModel()

    private val adapter: SampleListAdapter by lazy(mode = LazyThreadSafetyMode.NONE) {
        Log.d("SampleListFragment", "Created new instance of adapter")
        SampleListAdapter(this, viewModel)
    }

    /**
     * Subscriptions to the view model need to be done in the [onCreate] method so they aren't created multiple times
     * if the fragment is put into the back stack. It is safe to access views from blocks inside the subscribers because
     * the state updates are only delivered when the view in the started state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.selectSubscribe(SampleListViewState::loading) { entities ->
            when (entities) {
                is Incomplete -> showMessage("Data loading...")
                is Error -> showMessage("Error loading data")
            }
        }
        viewModel.selectSubscribe(SampleListViewState::entities) {
            adapter.updateData(it)
        }
        viewModel.subscribeMessages {
            Log.d("Messages", "Got message: $it")
            showMessage(it.toString())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sample_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        // Synchronously get the state from the ViewModel to initialize the adapter with some data. This will allow
        // the view to be restored with the scroll position maintained.
        withState(viewModel) {
            if (it.loading is Success) {
                adapter.updateDataImmediate(it.entities)
            }
        }
        recyclerView.adapter = adapter

        stateUpdate.setOnClickListener {
            viewModel.simulateStateUpdate()
        }

        nextFragment.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, EmptyFragment())
                .addToBackStack("Empty fragment")
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView.adapter = null
    }

    private fun showMessage(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}