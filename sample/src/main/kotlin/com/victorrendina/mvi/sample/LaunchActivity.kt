package com.victorrendina.mvi.sample

import android.content.Intent
import android.os.Bundle
import com.victorrendina.mvi.extensions.addArguments
import com.victorrendina.mvi.sample.counter.CounterActivity
import com.victorrendina.mvi.sample.counter.CounterArgs
import com.victorrendina.mvi.sample.fancylist.FancyListActivity
import com.victorrendina.mvi.sample.framework.BaseActivity
import com.victorrendina.mvi.sample.list.SampleListActivity
import com.victorrendina.mvi.sample.resetables.ResettableActivity
import kotlinx.android.synthetic.main.activity_launch.*

class LaunchActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        counterButton.setOnClickListener {
            val intent = Intent(this, CounterActivity::class.java).addArguments(CounterArgs(5))
            startActivity(intent)
        }

        listButton.setOnClickListener {
            val intent = Intent(this, SampleListActivity::class.java)
            startActivity(intent)
        }

        resetableButton.setOnClickListener {
            val intent = Intent(this, ResettableActivity::class.java)
            startActivity(intent)
        }

        fancyListButton.setOnClickListener {
            val intent = Intent(this, FancyListActivity::class.java)
            startActivity(intent)
        }
    }
}
