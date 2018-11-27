package com.victorrendina.mvi.sample.swipemenu

import android.os.Bundle
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseActivity

class SwipeMenuActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, SwipeMenuFragment())
                .commit()
        }
    }

}