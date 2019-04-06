package com.victorrendina.mvi

import androidx.lifecycle.LifecycleOwner
import kotlin.reflect.KProperty1

interface MviView : LifecycleOwner {

    /**
     * Subscribes to all state updates for the given viewModel.
     */
    fun <S : MviState, A : MviArgs> BaseMviViewModel<S, A>.subscribe(subscriber: (S) -> Unit) {
        subscribe(this@MviView, subscriber)
    }

    /**
     * Subscribes to the message queue for the given viewModel.
     */
    fun <S : MviState, A : MviArgs> BaseMviViewModel<S, A>.subscribeMessages(subscriber: (Any) -> Unit) {
        subscribeMessages(this@MviView, subscriber)
    }

    /**
     * Subscribes to state changes for only a specific property and calls the subscribe with
     * only that single property.
     */
    fun <S : MviState, A : MviArgs, P> BaseMviViewModel<S, A>.selectSubscribe(
        prop1: KProperty1<S, P>,
        subscriber: (P) -> Unit
    ) = selectSubscribe(this@MviView, prop1, subscriber)

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     */
    fun <S : MviState, A : MviArgs, T> BaseMviViewModel<S, A>.asyncSubscribe(
        asyncProp: KProperty1<S, Async<T>>,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = asyncSubscribe(this@MviView, asyncProp, onFail, onSuccess)

    /**
     * Subscribes to state changes for two properties.
     */
    fun <S : MviState, A : MviArgs, P1, P2> BaseMviViewModel<S, A>.selectSubscribe(
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        subscriber: (P1, P2) -> Unit
    ) = selectSubscribe(this@MviView, prop1, prop2, subscriber)

    /**
     * Subscribes to state changes for three properties.
     */
    fun <S : MviState, A : MviArgs, P1, P2, P3> BaseMviViewModel<S, A>.selectSubscribe(
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        prop3: KProperty1<S, P3>,
        subscriber: (P1, P2, P3) -> Unit
    ) = selectSubscribe(this@MviView, prop1, prop2, prop3, subscriber)

    /**
     * Subscribes to state changes for four properties.
     */
    fun <S : MviState, A : MviArgs, P1, P2, P3, P4> BaseMviViewModel<S, A>.selectSubscribe(
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        prop3: KProperty1<S, P3>,
        prop4: KProperty1<S, P4>,
        subscriber: (P1, P2, P3, P4) -> Unit
    ) = selectSubscribe(this@MviView, prop1, prop2, prop3, prop4, subscriber)
}