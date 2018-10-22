package com.victorrendina.mvi.sample.counter

import android.os.Bundle
import com.victorrendina.mvi.extensions.addArguments
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseActivity
import com.victorrendina.mvi.extensions.viewModel

class CounterActivity : BaseActivity() {

    /**
     * Activity view model that is shared with fragments that use activityViewModel(). View model will exist until the
     * activity is finished.
     */
    private val viewModel: CounterViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                // Create a fragment an add the mvi arguments for an initial count
                .add(R.id.container, CounterFragment().addArguments(CounterArgs(2)))
                .commit()
        }
    }
}
