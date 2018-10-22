package com.victorrendina.mvi.sample

import android.content.res.Resources
import android.util.Log
import javax.inject.Inject

class SampleDep @Inject constructor(
    private val resources: Resources
) {

    fun sayHello() {
        Log.d(this::class.java.simpleName, "Hello from ${resources.getString(R.string.app_name)}")
    }

}