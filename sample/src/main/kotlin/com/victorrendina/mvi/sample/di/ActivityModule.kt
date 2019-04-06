package com.victorrendina.mvi.sample.di

import com.victorrendina.mvi.sample.LaunchActivity
import com.victorrendina.mvi.sample.framework.BaseFragmentActivity
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
    abstract fun fragmentActivity(): BaseFragmentActivity
}