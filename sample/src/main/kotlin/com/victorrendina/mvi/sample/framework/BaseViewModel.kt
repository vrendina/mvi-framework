package com.victorrendina.mvi.sample.framework

import com.squareup.leakcanary.LeakCanary
import com.victorrendina.mvi.BaseMviViewModel
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.sample.BuildConfig

abstract class BaseViewModel<S : MviState, A : MviArgs>(initialState: S, arguments: A?) :
    BaseMviViewModel<S, A>(
        initialState = initialState,
        arguments = arguments,
        debugMode = BuildConfig.DEBUG
    ) {

    override fun onCleared() {
        super.onCleared()
        LeakCanary.installedRefWatcher().watch(this)
    }
}