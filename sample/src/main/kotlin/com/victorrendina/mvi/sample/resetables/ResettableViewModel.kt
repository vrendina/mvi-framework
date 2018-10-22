package com.victorrendina.mvi.sample.resetables

import android.util.Log
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.annotations.MviViewModel
import com.victorrendina.mvi.sample.framework.BaseViewModel
import com.victorrendina.mvi.util.ValueLatch
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

data class ResettableViewState(
    val toggleState: Boolean = false,
    val sliderPosition: Int = 50
) : MviState

@MviViewModel
class ResettableViewModel(
    initialState: ResettableViewState
) : BaseViewModel<ResettableViewState, MviArgs>(initialState, null) {

    private val toggleLatch = ValueLatch(false,
        sender = ::sendToggleStateToHost,
        listener = { valueToShow ->
            setState { copy(toggleState = valueToShow) }
        })

    private val sliderLatch = ValueLatch(50, 250, 5000,
        sender = ::sendSliderStateToHost,
        listener = {
            setState { copy(sliderPosition = it) }
        })

    init {
        logStateChanges()
        // Dispose any latches when the view model is cleared
        toggleLatch.disposeOnClear()
        sliderLatch.disposeOnClear()
    }

    fun updateSliderFromUi(position: Int) {
        sliderLatch.setUiValue(position)
    }

    fun updateToggleFromUi(state: Boolean) {
        toggleLatch.setUiValue(state)
    }

    private fun sendToggleStateToHost(state: Boolean) {
        Log.d(tag, "Sending new toggle state update $state")
    }

    private fun sendSliderStateToHost(position: Int) {
        Log.d(tag, "Sending new slider state update $position")
    }

    fun simulateStateUpdate() {
        // Delay the state update for one second
        withState { state ->
            Observable.timer(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    toggleLatch.setStateValue(state.toggleState)
                    sliderLatch.setStateValue(state.sliderPosition)
                }
        }
    }
}