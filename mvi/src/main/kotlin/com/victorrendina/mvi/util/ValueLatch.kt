package com.victorrendina.mvi.util

import android.os.Looper
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import java.util.concurrent.TimeUnit

class ValueLatch<T>(
    value: T,
    private val sendDelay: Long = DEFAULT_SEND_DELAY,
    private val restoreDelay: Long = DEFAULT_RESTORE_DELAY,
    private val sender: (valueToSend: T) -> Unit,
    private val listener: (valueToShow: T) -> Unit
) : Disposable {

    private var uiValue: T = value
    private var stateValue: T = value
    private var suppressUpdates = false

    private val disposables = CompositeDisposable()

    /**
     * Sets the value that should be reflected by the user interface. Calling this method immediately triggers a call to
     * the [listener] which should be used to update the user interface. If a state update with the provided value is not
     * received after [sendDelay] + [restoreDelay] then the [listener] will be called again with the previous [stateValue].
     */
    fun setUiValue(value: T) {
        enforceMainThread()
        if (!isDisposed) {
            cancelPendingActions()

            uiValue = value
            suppressUpdates = true

            // Schedule value sending
            Observable.timer(sendDelay, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { sender(value) }.addTo(disposables)

            // Schedule value restoration
            Observable.timer(sendDelay + restoreDelay, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { restoreUiValue() }.addTo(disposables)

            listener(value)
        }
    }

    /**
     * Sets the [stateValue]. This method should be called whenever a state update is received from the backend. Calling this
     * method will prevent the user interface from being restored to the previous state value.
     */
    fun setStateValue(value: T) {
        enforceMainThread()
        if (!isDisposed) {
            stateValue = value

            if (!suppressUpdates) {
                restoreUiValue()
            }
        }
    }

    private fun restoreUiValue() {
        suppressUpdates = false
        uiValue = stateValue
        listener(uiValue)
    }

    private fun cancelPendingActions() {
        disposables.clear()
    }

    private fun enforceMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw IllegalAccessException("Latch methods can only be called safely from the main thread.")
        }
    }

    override fun isDisposed(): Boolean = disposables.isDisposed

    override fun dispose() {
        disposables.dispose()
    }

    companion object {
        const val DEFAULT_SEND_DELAY = 0L
        const val DEFAULT_RESTORE_DELAY = 3000L
    }
}