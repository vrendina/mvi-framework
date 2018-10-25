package com.victorrendina.mvi.util

import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import java.util.concurrent.TimeUnit

open class ValueThrottler<T>(
    interval: Long = 250,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    scheduler: Scheduler = AndroidSchedulers.mainThread(),
    listener: (T) -> Unit
) : Disposable {

    private val values: Subject<T> = PublishSubject.create<T>().toSerialized()
    private val subscription: Disposable

    init {
        subscription = values.throttleLast(interval, unit)
            .observeOn(scheduler)
            .subscribe(listener)
    }

    /**
     * Emit a value that will be throttled. It is safe to call this method from multiple threads.
     */
    fun emitValue(value: T) {
        if (!isDisposed) {
            values.onNext(value)
        }
    }

    override fun isDisposed(): Boolean = subscription.isDisposed

    override fun dispose() {
        subscription.dispose()
    }
}