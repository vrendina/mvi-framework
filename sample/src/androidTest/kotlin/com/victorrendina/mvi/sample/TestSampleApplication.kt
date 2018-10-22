package com.victorrendina.mvi.sample

import com.victorrendina.mvi.sample.di.DaggerTestAppComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication

class TestSampleApplication: MviSampleApplication() {

    override fun installLeakCanary() {
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerTestAppComponent.builder().create(this)
    }
}