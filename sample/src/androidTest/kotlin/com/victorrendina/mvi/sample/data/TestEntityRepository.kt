package com.victorrendina.mvi.sample.data

import io.reactivex.Single
import java.util.UUID
import javax.inject.Inject

class TestEntityRepository @Inject constructor(): EntityRepository {

    override fun getEntities(scope: String): Single<List<Entity>> {
        return Single.just(listOf(testEntity(), testEntity(), testEntity()))
    }

    private fun testEntity() = Entity("Test" + generateString(), "Test" + generateString())

    private fun generateString() = UUID.randomUUID().toString()
}