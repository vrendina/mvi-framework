package com.victorrendina.mvi

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

internal class WorkerMviStateStore<S : MviState>(initialState: S) : MviStateStore<S> {

    private val scheduler = Schedulers.newThread()
    private val worker = scheduler.createWorker()

    private val subject: BehaviorSubject<S> = BehaviorSubject.createDefault(initialState)
    private val queue = MviStateQueue<S>()

    override val observable: Observable<S> = subject.distinctUntilChanged()

    override val state: S
        get() = subject.value!!

    /**
     * Get the current state. The block of code is posted to a queue and all pending set state blocks
     * are guaranteed to run before the get block is run.
     */
    override fun get(block: (S) -> Unit) {
        queue.enqueueGetStateBlock(block)
        worker.schedule { flushQueues() }
    }

    /**
     * Call this to update the state. The state reducer will get added to a queue that is processes
     * on a background thread. The state reducer's receiver type is the current state when the
     * reducer is called.
     */
    override fun set(reducer: S.() -> S) {
        queue.enqueueSetStateBlock(reducer)
        worker.schedule { flushQueues() }
    }

    private fun flushQueues() {
        // Execute any pending state updates first
        runSetStateBlocks()
        // If there is a get state block
        val block = queue.dequeueGetStateBlock() ?: return
        block(state)
        flushQueues()
    }

    private fun runSetStateBlocks() {
        val blocks = queue.dequeueAllSetStateBlocks() ?: return
        blocks.fold(state) { state, reducer -> state.reducer() }.run { subject.onNext(this) }
    }

    override fun isDisposed(): Boolean = worker.isDisposed

    override fun dispose() {
        worker.dispose()
        scheduler.shutdown()
    }
}