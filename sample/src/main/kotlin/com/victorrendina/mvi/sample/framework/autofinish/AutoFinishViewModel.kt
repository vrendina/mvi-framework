package com.victorrendina.mvi.sample.framework.autofinish

import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.annotations.MviViewModel
import com.victorrendina.mvi.di.InjectableViewModelFactory
import com.victorrendina.mvi.sample.framework.BaseViewModel
import com.victorrendina.rxqueue2.QueueSubject
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Provider

data class AutoFinishViewState(
    val enabled: Boolean = true,
    val registeredKeys: Set<AutoFinishKey> = emptySet()
) : MviState

@MviViewModel
class AutoFinishViewModel(
    initialState: AutoFinishViewState,
    private val autoFinishStream: AutoFinishStream
) : BaseViewModel<AutoFinishViewState>(initialState) {

    private val eventQueue: QueueSubject<AutoFinishEvent> = QueueSubject.create()

    init {
        logStateChanges()
        autoFinishStream.observeEvents().subscribe { key ->
            withState { state ->
                if (state.enabled && state.registeredKeys.contains(key)) {
                    eventQueue.onNext(AutoFinishEvent(key))
                }
            }
        }.disposeOnClear()
    }

    fun observeFinishEvents(): Observable<AutoFinishEvent> = eventQueue

    fun registerKey(key: AutoFinishKey) {
        setState {
            copy(registeredKeys = registeredKeys + key)
        }
    }

    fun unregisterKey(key: AutoFinishKey) {
        setState {
            copy(registeredKeys = registeredKeys - key)
        }
    }

    fun setEnabled(enabled: Boolean) {
        setState {
            copy(enabled = enabled)
        }
    }

    fun emitKey(key: AutoFinishKey) {
        autoFinishStream.emitKey(key)
    }

    data class AutoFinishEvent(val key: AutoFinishKey)

    class Factory @Inject constructor(
        private val autoFinishStream: Provider<AutoFinishStream>
    ) : InjectableViewModelFactory<AutoFinishViewModel, AutoFinishViewState, MviArgs> {
        override fun create(initialState: AutoFinishViewState, arguments: MviArgs?): AutoFinishViewModel {
            return AutoFinishViewModel(initialState, autoFinishStream.get())
        }
    }
}