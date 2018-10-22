package com.victorrendina.mvi.util

import com.victorrendina.mvi.BaseTest
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class ValueLatchTest : BaseTest() {

    private val latch = ValueLatch(
        TEST_INITIAL_VALUE, TEST_SEND_DELAY, TEST_RESTORE_DELAY,
        sender = {
            senderCount++
            lastSenderValue = it
        },
        listener = {
            listenerCount++
            lastListenerValue = it
        })

    private var listenerCount = 0
    private var lastListenerValue: String? = null
    private var senderCount = 0
    private var lastSenderValue: String? = null

    private val testScheduler = TestScheduler()

    @Before
    fun setUp() {
        RxJavaPlugins.setComputationSchedulerHandler { testScheduler }
    }

    @Test
    fun `Ui Value is Delivered Immediately When Set`() {
        assertNull(lastListenerValue)
        assertEquals(0, listenerCount)

        val testValue = "Testing 1"
        latch.setUiValue(testValue)

        assertEquals(1, listenerCount)
        assertEquals(testValue, lastListenerValue)
    }

    @Test
    fun `Ui Value is Restored After Delay`() {
        assertNull(lastListenerValue)
        assertEquals(0, listenerCount)

        val testValue = "Testing 1"
        latch.setUiValue(testValue)

        assertEquals(1, listenerCount)
        assertEquals(testValue, lastListenerValue)

        testScheduler.advanceTimeBy(TEST_SEND_DELAY + TEST_RESTORE_DELAY, TimeUnit.MILLISECONDS)

        assertEquals(2, listenerCount)
        assertEquals(TEST_INITIAL_VALUE, lastListenerValue)
    }

    @Test
    fun `State Value is Sent After Delay`() {
        assertNull(lastSenderValue)
        assertEquals(0, senderCount)

        val testValue = "Testing 1"
        latch.setUiValue(testValue)

        assertNull(lastSenderValue)
        assertEquals(0, senderCount)

        testScheduler.advanceTimeBy(TEST_SEND_DELAY, TimeUnit.MILLISECONDS)

        assertEquals(1, senderCount)
        assertEquals(testValue, lastSenderValue)
    }

    @Test
    fun `State Value Updates Ui Value When Not Suppressed`() {
        assertNull(lastListenerValue)
        assertEquals(0, listenerCount)

        val testValue = "Testing 1"
        latch.setStateValue(testValue)

        assertEquals(1, listenerCount)
        assertEquals(testValue, lastListenerValue)
    }

    @Test
    fun `Set Ui Value on Thread Other than Main Fails`() {
        var exception: Exception? = null
        thread {
            try {
                latch.setUiValue("fail")
            } catch (e: Exception) {
                exception = e
            }
        }.join()

        assertNotNull(exception)
    }

    @Test
    fun `Set State Value on Thread Other than Main Fails`() {
        var exception: Exception? = null
        thread {
            try {
                latch.setStateValue("fail")
            } catch (e: Exception) {
                exception = e
            }
        }.join()

        assertNotNull(exception)
    }

    companion object {
        private const val TEST_SEND_DELAY = 250L
        private const val TEST_RESTORE_DELAY = 1000L
        private const val TEST_INITIAL_VALUE = "Initial"
    }
}