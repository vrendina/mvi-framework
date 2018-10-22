package com.victorrendina.mvi.sample.list

import android.os.Bundle
import com.victorrendina.mvi.extensions.addArguments
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseActivity

class SampleListActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, SampleListFragment().addArguments(SampleListArgs("Argument Scope", 2)))
                .commit()
        }
    }
}
