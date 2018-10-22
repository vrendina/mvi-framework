package com.victorrendina.mvi.sample.list

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.victorrendina.mvi.sample.BaseAndroidUnitTest
import com.victorrendina.mvi.sample.data.Entity
import com.victorrendina.mvi.sample.data.EntityRepository
import io.reactivex.Single
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString

class SampleListViewModelTest: BaseAndroidUnitTest() {

    private val testEntity = Entity("testId", "test")

    private val repositoryMock = mock<EntityRepository> {
        on { getEntities(anyString())} doReturn Single.just(listOf(testEntity))
    }

    private val viewModel = SampleListViewModel(SampleListViewState(), null, repositoryMock)

    @Test
    fun `Smoke Test`() {
    }

}