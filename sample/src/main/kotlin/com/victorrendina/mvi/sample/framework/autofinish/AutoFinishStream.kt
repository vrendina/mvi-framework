package com.victorrendina.mvi.sample.framework.autofinish

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [AutoFinishStream] provides a global event stream used to close multiple activities simultaneously that
 * have registered for the same finish keys.
 */
@Singleton
class AutoFinishStream @Inject constructor() {

    private val eventStream: PublishSubject<AutoFinishKey> = PublishSubject.create()

    fun observeEvents(): Observable<AutoFinishKey> = eventStream

    fun emitKey(key: AutoFinishKey) {
        eventStream.onNext(key)
    }

}