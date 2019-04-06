package com.victorrendina.mvi.sample.framework

import android.os.Bundle
import com.victorrendina.mvi.MviView
import com.victorrendina.mvi.di.MviViewModelFactory
import com.victorrendina.mvi.di.MviViewModelFactoryOwner
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

abstract class BaseActivity : DaggerAppCompatActivity(), MviView, MviViewModelFactoryOwner {

    @Inject
    override lateinit var viewModelFactory: MviViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        animateActivityEnter()
        super.onCreate(savedInstanceState)
    }

    protected open fun animateActivityEnter() {
    }

    protected open fun animateActivityPopExit() {
    }

    override fun finish() {
        super.finish()
        animateActivityPopExit()
    }
}