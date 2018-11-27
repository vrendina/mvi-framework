package com.victorrendina.mvi.sample.counter

import android.content.res.Resources
import android.util.Log
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.annotations.MviViewModel
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseViewModel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CounterArgs(val initialCount: Int) : MviArgs

data class CounterViewState(val count: Int = 0) : MviState {
    // This constructor will automatically be called to create the initial view state if arguments of type CounterArgs
    // have been passed to the view.
    @Suppress("unused")
    constructor(arguments: CounterArgs) : this(count = arguments.initialCount)
}

@MviViewModel
class CounterViewModel(
    initialState: CounterViewState,
    arguments: CounterArgs?,
    resources: Resources
) : BaseViewModel<CounterViewState, CounterArgs>(initialState, arguments) {

    init {
        Log.d(tag, "Initialized: $this")
        Log.d(tag, "Arguments $arguments")
        Log.d(tag, "Resources: ${resources.getString(R.string.app_name)}")
        logStateChanges()
    }

    fun increaseCount() {
        setState {
            Log.d(tag, "Reducer running on ${Thread.currentThread().name}")
            copy(count = count + 1)
        }

        withState {
            Log.d(tag, "The current count is ${it.count}")
        }
    }

    fun decreaseCount() {
        setState {
            copy(count = count - 1)
        }
    }
}