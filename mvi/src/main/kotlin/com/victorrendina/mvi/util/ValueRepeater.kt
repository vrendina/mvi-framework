package com.victorrendina.mvi.util

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class ValueRepeater<T>(
    private val initialDelay: Long = 0,
    private val interval: Long = 250,
    private val unit: TimeUnit = TimeUnit.MILLISECONDS,
    private val scheduler: Scheduler = AndroidSchedulers.mainThread(),
    private val listener: (value: T, count: Long) -> Unit
): Disposable {

    private var subscription: Disposable? = null
    private val disposed = AtomicBoolean(false)

    /**
     * Repeatedly send the provided value.
     */
    fun startEmissions(value: T) {
        if (!isDisposed) {
            stopEmissions()
            subscription = Observable.interval(initialDelay, interval, unit)
                .observeOn(scheduler)
                .subscribe { listener(value, it) }
        }
    }

    fun stopEmissions() {
        subscription?.dispose()
    }

    override fun isDisposed(): Boolean = disposed.get()

    override fun dispose() {
        disposed.set(true)
        stopEmissions()
    }

}