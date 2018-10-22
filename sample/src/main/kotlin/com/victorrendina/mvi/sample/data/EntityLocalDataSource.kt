package com.victorrendina.mvi.sample.data

import io.reactivex.Single
import java.util.UUID
import javax.inject.Inject

class EntityLocalDataSource @Inject constructor() {

    /**
     * Provide some fake entity data.
     */
    fun getEntities(scope: String, count: Int = 250): Single<List<Entity>> {
        return Single.fromCallable {
            // Simulate some latency
            Thread.sleep(1000)
            val entities = ArrayList<Entity>()
            for (i in 0 until count) {
                entities.add(randomEntity())
            }
            entities
        }
    }

    private fun randomEntity() = Entity(generateString(), generateString())

    private fun generateString() = UUID.randomUUID().toString()
}