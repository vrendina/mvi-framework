package com.victorrendina.mvi.sample.data

import org.junit.Test

class EntityLocalDataSourceTest {

    private val localDataSource = EntityLocalDataSource()

    @Test
    fun `Get Entities Returns Correct Count`() {
        val count = 25
        val result = localDataSource.getEntities("some scope", count)

        result.test()
            .assertComplete()
            .assertValueCount(1)
            .assertValue { it.size == count }
    }
}