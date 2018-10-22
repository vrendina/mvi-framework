package com.victorrendina.mvi.sample.data

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Single
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString

class ProductionEntityRepositoryTest {

    private val testEntity = Entity("testId", "test")

    private val localDataSource = mock<EntityLocalDataSource> {
        on { getEntities(anyString(), anyInt())} doReturn Single.just(listOf(testEntity))
    }

    private val repository = ProductionEntityRepository(localDataSource)

    @Test
    fun `Get Entities Calls Local Data Source`() {
        val scope = "someScope"
        repository.getEntities(scope)
        verify(localDataSource, times(1)).getEntities(scope)
    }

    @Test
    fun `Get Entities Returns Local Data`() {
        repository.getEntities("someScope").test()
            .assertValue { it[0] == testEntity }
    }

}