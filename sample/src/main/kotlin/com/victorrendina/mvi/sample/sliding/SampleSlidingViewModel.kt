package com.victorrendina.mvi.sample.sliding

import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.annotations.MviViewModel
import com.victorrendina.mvi.di.InjectableViewModelFactory
import com.victorrendina.mvi.sample.framework.BaseViewModel
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SampleSlidingViewState(
    val count: Long = 0
) : MviState

@MviViewModel
class SampleSlidingViewModel(
    initialState: SampleSlidingViewState
) : BaseViewModel<SampleSlidingViewState>(initialState) {


    init {
        Observable.interval(500, TimeUnit.MILLISECONDS).subscribe {
            setState {
                copy(count = it)
            }
        }.disposeOnClear()
    }

    class Factory @Inject constructor() :
        InjectableViewModelFactory<SampleSlidingViewModel, SampleSlidingViewState, MviArgs> {
        override fun create(initialState: SampleSlidingViewState, arguments: MviArgs?): SampleSlidingViewModel {
            return SampleSlidingViewModel(initialState)
        }
    }
}