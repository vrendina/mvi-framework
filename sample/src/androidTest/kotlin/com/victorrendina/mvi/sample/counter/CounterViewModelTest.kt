package com.victorrendina.mvi.sample.counter

import com.victorrendina.mvi.sample.BaseAndroidTest
import com.victorrendina.mvi.withState
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterViewModelTest : BaseAndroidTest() {

    private val viewModel = CounterViewModel(CounterViewState(34), null)

    @Test
    fun increase_count_updates_state() {
        viewModel.increaseCount()
        withState(viewModel) {
            assertTrue(it.count == 35)
        }
    }

    @Test
    fun decrease_count_updates_state() {
        viewModel.decreaseCount()
        withState(viewModel) {
            assertTrue(it.count == 33)
        }
    }
}