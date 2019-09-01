package com.victorrendina.mvi.sample.framework.autofinish

interface AutoFinishHost {

    /**
     * Register to listen for a finish key. If a finish key event is emitted that matches
     * the registered finish key then the host will close.
     *
     * @param key key to listen to
     */
    fun registerKey(key: AutoFinishKey)

    /**
     * No longer listen to events that match the given key.
     *
     * @param key key to unregister
     */
    fun unregisterKey(key: AutoFinishKey)

    /**
     * Close any activities that are registered for the provided finish key by emitting a finish key event.
     */
    fun emitKey(key: AutoFinishKey)

    /**
     * Set whether or not auto finish should be enabled or disabled. If disabled, all key events matching registered
     * keys will be ignored.
     *
     * @param enabled if auto finish should be enabled
     */
    fun setEnabled(enabled: Boolean)

}