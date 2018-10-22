package com.victorrendina.mvi.sample.framework

import com.victorrendina.mvi.MviView
import com.victorrendina.mvi.di.MviViewModelFactory
import com.victorrendina.mvi.di.MviViewModelFactoryOwner
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), MviView, MviViewModelFactoryOwner {

    @Inject
    override lateinit var viewModelFactory: MviViewModelFactory
}