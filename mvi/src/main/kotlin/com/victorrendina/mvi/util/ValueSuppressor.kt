package com.victorrendina.mvi.util

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class ValueSuppressor<T>(
    private val delay: Long = DEFAULT_SUPPRESSION_DELAY,
    private val unit: TimeUnit = TimeUnit.MILLISECONDS,
    private val scheduler: Scheduler = AndroidSchedulers.mainThread(),
    private val listener: (T) -> Unit
) : Disposable {

    private var value: T? = null
    private var suppressUpdates = false

    private var disposed = false
    private var disposable: Disposable? = null

    /**
     * Start suppressing updates to the value. After the [delay] expires if a value has been set
     * the [listener] will be called on [scheduler] with the latest value.
     *
     * Each time this method is called the delay will restart.
     */
    @Synchronized
    fun startSuppression() {
        if (!isDisposed) {
            cancelPendingActions()

            suppressUpdates = true

            disposable = Observable.timer(delay, unit)
                .observeOn(scheduler).subscribe { stopSuppression() }
        }
    }

    /**
     * Force suppression to stop immediately and provide the [listener] with the most recent value
     * on the target [scheduler] if the value exists.
     */
    @Synchronized
    fun stopSuppression() {
        if (!isDisposed) {
            cancelPendingActions()
            suppressUpdates = false
            notifyListener()
        }
    }

    /**
     * Set the value and immediately call the [listener] unless values are suppressed. If values are currently
     * being suppressed the value will be delivered after the delay has expired. The listener will
     * be called on the target [scheduler] regardless of what thread this method is invoked from.
     */
    @Synchronized
    fun setValue(value: T) {
        if (!isDisposed) {
            this.value = value

            if (!suppressUpdates) {
                notifyListener()
            }
        }
    }

    @Synchronized
    fun getValue(): T? = value

    private fun notifyListener() {
        value?.also {
            scheduler.scheduleDirect {
                if (!disposed) {
                    listener(it)
                }
            }
        }
    }

    private fun cancelPendingActions() {
        disposable?.dispose()
    }

    @Synchronized
    override fun isDisposed(): Boolean = disposed

    @Synchronized
    override fun dispose() {
        disposed = true
        cancelPendingActions()
    }

    companion object {
        const val DEFAULT_SUPPRESSION_DELAY = 3000L // 3 second delay by default
    }
}