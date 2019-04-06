package com.victorrendina.mvi

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.internal.disposables.DisposableHelper
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.LambdaObserver
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * An wrapper around an [Observer] associated with a [LifecycleOwner]. It has an [activeState], and when in a lifecycle state greater
 * than the [activeState] (as defined by [Lifecycle.State.isAtLeast()]) it will deliver values to the [sourceObserver] or [onNext] lambda.
 * When in a lower lifecycle state, the most recent update will be saved, and delivered when active again.
 */
internal class MviLifecycleAwareObserver<T>(
    private var owner: LifecycleOwner?,
    private val activeState: Lifecycle.State = DEFAULT_ACTIVE_STATE,
    private val alwaysDeliverLastValueWhenUnlocked: Boolean = false,
    private var sourceObserver: Observer<T>?,
    private var destroyCallback: ((MviLifecycleAwareObserver<T>) -> Unit)? = null
) : AtomicReference<Disposable>(), LifecycleObserver, Observer<T>,
    Disposable {

    constructor(
        owner: LifecycleOwner,
        activeState: Lifecycle.State = DEFAULT_ACTIVE_STATE,
        alwaysDeliverLastValueWhenUnlocked: Boolean = false,
        onComplete: Action = Functions.EMPTY_ACTION,
        onSubscribe: Consumer<in Disposable> = Functions.emptyConsumer(),
        onError: Consumer<in Throwable> = Functions.ON_ERROR_MISSING,
        onNext: Consumer<T> = Functions.emptyConsumer(),
        destroyCallback: ((MviLifecycleAwareObserver<T>) -> Unit)? = null
    ) : this(
        owner,
        activeState,
        alwaysDeliverLastValueWhenUnlocked,
        LambdaObserver<T>(onNext, onError, onComplete, onSubscribe),
        destroyCallback
    )

    private var lastUndeliveredValue: T? = null
    private var lastValue: T? = null
    private val locked = AtomicBoolean(true)

    override fun onSubscribe(d: Disposable) {
        if (DisposableHelper.setOnce(this, d)) {
            requireOwner().lifecycle.addObserver(this)
            requireSourceObserver().onSubscribe(this)
        }
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
        updateLock()
    }

    private fun updateLock() {
        if (owner?.lifecycle?.currentState?.isAtLeast(activeState) == true) {
            unlock()
        } else {
            lock()
        }
    }

    override fun onNext(t: T) {
        if (!locked.get()) {
            requireSourceObserver().onNext(t)
        } else {
            lastUndeliveredValue = t
        }
        lastValue = t
    }

    override fun onError(e: Throwable) {
        if (!isDisposed) {
            lazySet(DisposableHelper.DISPOSED)
            requireSourceObserver().onError(e)
        }
    }

    override fun onComplete() {
        requireSourceObserver().onComplete()
    }

    override fun isDisposed(): Boolean = get() === DisposableHelper.DISPOSED

    override fun dispose() {
        owner?.lifecycle?.removeObserver(this)
        owner = null
        sourceObserver = null
        destroyCallback = null
        DisposableHelper.dispose(this)
    }

    private fun unlock() {
        if (!locked.getAndSet(false)) {
            return
        }
        if (!isDisposed) {
            val valueToDeliverOnUnlock =
                if (alwaysDeliverLastValueWhenUnlocked && lastValue != null) lastValue else lastUndeliveredValue
            lastUndeliveredValue = null
            if (valueToDeliverOnUnlock != null) {
                onNext(valueToDeliverOnUnlock)
            }
        }
    }

    private fun lock() {
        locked.set(true)
    }

    private fun requireOwner(): LifecycleOwner =
        requireNotNull(owner) { "Cannot access lifecycleOwner after onDestroy." }

    private fun requireSourceObserver() = requireNotNull(sourceObserver) { "Cannot access observer after onDestroy." }

    companion object {
        private val DEFAULT_ACTIVE_STATE = Lifecycle.State.STARTED
    }
}