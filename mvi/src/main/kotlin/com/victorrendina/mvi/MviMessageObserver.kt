package com.victorrendina.mvi

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper
import java.util.concurrent.atomic.AtomicReference

internal class MviMessageObserver(
    owner: LifecycleOwner,
    private val observable: Observable<Any>,
    subscriber: ((Any) -> Unit),
    private var destroyCallback: ((MviMessageObserver) -> Unit)? = null

) : AtomicReference<Disposable>(), LifecycleObserver, Disposable {

    private var owner: LifecycleOwner? = owner
    private var subscriber: ((Any) -> Unit)? = subscriber
    private var subscription: Disposable? = null

    init {
        owner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        destroyCallback?.invoke(this)
        if (!isDisposed) {
            dispose()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    fun onLifecycleEvent() {
        updateSubscription()
    }

    private fun updateSubscription() {
        if (owner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true) {
            synchronized(this) {
                if (!isDisposed && subscription == null) {
                    subscription = observable.subscribe(subscriber)
                }
            }
        } else {
            innerDispose()
        }
    }

    @Synchronized
    private fun innerDispose() {
        subscription?.dispose()
        subscription = null
    }

    override fun isDisposed(): Boolean = get() === DisposableHelper.DISPOSED

    @Synchronized
    override fun dispose() {
        owner?.lifecycle?.removeObserver(this)
        owner = null
        subscriber = null
        destroyCallback = null
        innerDispose()
        DisposableHelper.dispose(this)
    }
}