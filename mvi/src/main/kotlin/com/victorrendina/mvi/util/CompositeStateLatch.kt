package com.victorrendina.mvi.util

import com.victorrendina.mvi.util.StateLatch.Companion.DEFAULT_RESTORE_DELAY
import com.victorrendina.mvi.util.StateLatch.Companion.DEFAULT_SEND_INTERVAL
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap

class CompositeStateLatch<K, V>(
    private val restoreDelay: Long = DEFAULT_RESTORE_DELAY,
    private val sendInterval: Long = DEFAULT_SEND_INTERVAL,
    private val restoreScheduler: Scheduler = AndroidSchedulers.mainThread(),
    private val sendScheduler: Scheduler = Schedulers.io(),
    private val listener: ((key: K, value: V) -> Unit)? = null
) : Disposable {

    private val latches = ConcurrentHashMap<K, StateLatch<V>>()

    @Volatile
    private var disposed = false

    fun setState(key: K, value: V) {
        if (!isDisposed) {
            getLatch(key).setState(value)
        }
    }

    fun getState(key: K): V? = latches[key]?.getState()

    fun sendState(key: K, updater: () -> Unit) {
        if (!isDisposed) {
            getLatch(key).sendState(updater)
        }
    }

    /**
     * Dispose of and remove any latches from the composite that are no longer needed.
     */
    @Synchronized
    fun removeInactive(activeKeys: Set<K>) {
        while (true) {
            val removedLatches = HashSet<K>()

            val count = latches.size
            latches.forEach { (key, latch) ->
                if (!activeKeys.contains(key)) {
                    latch.dispose()
                    removedLatches.add(key)
                }
            }

            if (removedLatches.isEmpty() && count == latches.size) {
                break
            }

            removedLatches.forEach { key ->
                latches.remove(key)
            }
        }
    }

    private fun getLatch(key: K): StateLatch<V> {
        return latches[key] ?: createLatch(key).also { latches[key] = it }
    }

    private fun createLatch(key: K): StateLatch<V> {
        return StateLatch(restoreDelay, sendInterval, restoreScheduler, sendScheduler) { value ->
            listener?.invoke(key, value)
        }
    }

    override fun isDisposed(): Boolean = disposed

    @Synchronized
    override fun dispose() {
        disposed = true

        while (true) {
            val disposedLatches = HashSet<K>()

            latches.forEach { (key, latch) ->
                latch.dispose()
                disposedLatches.add(key)
            }
            disposedLatches.forEach { key ->
                latches.remove(key)
            }

            if (latches.isEmpty()) {
                break
            }
        }
    }
}
