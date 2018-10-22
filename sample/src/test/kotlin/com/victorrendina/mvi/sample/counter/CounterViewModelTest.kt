package com.victorrendina.mvi.sample.counter

import android.content.res.Resources
import com.nhaarman.mockitokotlin2.mock
import com.victorrendina.mvi.sample.BaseAndroidUnitTest
import com.victorrendina.mvi.withState
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterViewModelTest: BaseAndroidUnitTest() {

    private val resourcesMock = mock<Resources>()

    private val viewModel = CounterViewModel(CounterViewState(34), null, resourcesMock)

    @Test
    fun `Increase Count Updates State`() {
        viewModel.increaseCount()
        withState(viewModel) {
            assertTrue(it.count == 35)
        }
    }

    @Test
    fun `Decrease Count Updates State`() {
        viewModel.decreaseCount()
        withState(viewModel) {
            assertTrue(it.count == 33)
        }
    }

}