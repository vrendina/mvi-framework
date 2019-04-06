package com.victorrendina.mvi.sample

import android.os.Bundle
import android.view.View
import com.victorrendina.mvi.sample.counter.CounterArgs
import com.victorrendina.mvi.sample.counter.CounterFragment
import com.victorrendina.mvi.sample.framework.BaseActivity
import com.victorrendina.mvi.sample.framework.BaseFragmentActivity
import com.victorrendina.mvi.sample.framework.nav.screen
import com.victorrendina.mvi.sample.tabs.SampleTabHostFragment
import kotlinx.android.synthetic.main.activity_launch.*

class LaunchActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        counterButton.setOnClickListener(this)
        tabsButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.counterButton -> {
                screen(CounterFragment::class.java) {
                    activity = BaseFragmentActivity::class.java
                    arguments = CounterArgs(initialCount = 64)
                }.apply { startActivity(this@LaunchActivity) }
            }
            R.id.tabsButton -> {
                screen(SampleTabHostFragment::class.java) {
                    activity = BaseFragmentActivity::class.java
                }.apply { startActivity(this@LaunchActivity) }
            }
        }
    }


}
