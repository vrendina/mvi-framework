package com.victorrendina.mvi.sample.framework.autofinish

/**
 * Keys used to automatically finish activities when emitting an event that contains the key.
 * Classes that implement this interface must have proper equals and hashcode implementations (typically data classes).
 */
interface AutoFinishKey

/**
 * Basic implementation of the finish key that uses a string.
 */
data class StringAutoFinishKey(
    val key: String
): AutoFinishKey