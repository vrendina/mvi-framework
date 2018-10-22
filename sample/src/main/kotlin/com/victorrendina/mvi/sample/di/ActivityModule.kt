package com.victorrendina.mvi.sample.di

import com.victorrendina.mvi.sample.LaunchActivity
import com.victorrendina.mvi.sample.counter.CounterActivity
import com.victorrendina.mvi.sample.list.SampleListActivity
import com.victorrendina.mvi.sample.resetables.ResettableActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * All activities must be added to the module in order to be injectable by Dagger.
 */
@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun launchActivity(): LaunchActivity

    @ContributesAndroidInjector
    abstract fun counterActivity(): CounterActivity

    @ContributesAndroidInjector
    abstract fun listActivity(): SampleListActivity

    @ContributesAndroidInjector
    abstract fun resetableActivity(): ResettableActivity

}