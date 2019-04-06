package com.victorrendina.mvi.util

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * When updating values on a remote source and waiting for a response it is often a best practice
 * to update the user interface immediately even though the remote value may not be modified yet.
 * If no response is received from the remote source within a certain time the user interface should be
 * restored to the previous state.
 *
 * A [StateLatch] provides two methods that facilitate this behavior. When the user interacts with the application and
 * changes the state, the user interface should be updated immediately and the [sendState] method should
 * be called to send the updated state to the remote source. Calling [sendState] has several implications:
 *
 * - If [sendState] is called multiple times within the send interval, only the last block of code will be executed.
 * - When [sendState] is called, any state updates that are received will be suppressed until after the restore delay
 * has expired.
 *
 * When a state update is received from the remote source [setState] should be called with the updated value.
 * Calling [setState] may call the listener immediately or after a time interval no greater than the restore delay.
 *
 * - If [sendState] was called was called recently and the restore delay has not expired the listener will
 * be called once the restore delay has expired.
 * - If [sendState] has not been called recently and updates are not suppressed the listener will be called
 * immediately and the user interface should be updated to reflect the new remote state.
 *
 * Note that if no state update is ever received and [setState] has not been called then the listener will not
 * be called after the restore delay because there will be no state value to restore back to.
 *
 * @param restoreDelay Amount of time to wait before restoring the previous state value
 * @param sendInterval Amount of time to wait between sending state updates
 * @param restoreScheduler Scheduler that the listener should be executed on. By default this
 * operates on the Android main thread.
 * @param sendScheduler Scheduler that the state sending lambda should be executed on. By default
 * this operates on the IO scheduler.
 * @param listener Method that will be called when a state update is received or when the
 * state is being restored to the previous value. This listener should be used to update the
 * user interface or in your call setState {} in your ViewModel to update the view state.
 */
class StateLatch<T>(
    restoreDelay: Long = DEFAULT_RESTORE_DELAY,
    sendInterval: Long = DEFAULT_SEND_INTERVAL,
    restoreScheduler: Scheduler = AndroidSchedulers.mainThread(),
    sendScheduler: Scheduler = Schedulers.io(),
    listener: ((T) -> Unit)? = null
) : Disposable {

    private val throttler = ValueThrottler<() -> Unit>(sendInterval, scheduler = sendScheduler) { it.invoke() }
    private val suppressor = ValueSuppressor<T>(
        delay = sendInterval + restoreDelay,
        scheduler = restoreScheduler,
        listener = { value -> listener?.invoke(value) }
    )

    private val disposables = CompositeDisposable()

    init {
        disposables.addAll(throttler, suppressor)
    }

    /**
     * Update the state value. If value updates are not currently suppressed then the listener will be
     * called immediately with the new state value.
     */
    fun setState(value: T) {
        suppressor.setValue(value)
    }

    fun getState(): T? = suppressor.getValue()

    /**
     * Send the updated state using the provided block of code. If this method is invoked multiple times within
     * the send interval only the last method will be executed.
     */
    fun sendState(updater: () -> Unit) {
        suppressor.startSuppression()
        throttler.emitValue(updater)
    }

    override fun isDisposed(): Boolean = disposables.isDisposed

    override fun dispose() {
        disposables.dispose()
    }

    companion object {
        const val DEFAULT_RESTORE_DELAY = 3000L // 3 seconds before restoring previous state
        const val DEFAULT_SEND_INTERVAL = 250L // 250 ms between sendState calls
    }
}