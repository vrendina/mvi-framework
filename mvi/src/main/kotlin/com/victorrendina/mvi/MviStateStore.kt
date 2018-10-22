package com.victorrendina.mvi

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

interface MviStateStore<S : MviState> : Disposable {
    val state: S
    fun get(block: (S) -> Unit)
    fun set(reducer: S.() -> S)
    val observable: Observable<S>
}