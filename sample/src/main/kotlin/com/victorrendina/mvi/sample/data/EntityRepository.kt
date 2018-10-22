package com.victorrendina.mvi.sample.data

import io.reactivex.Single

interface EntityRepository {
    fun getEntities(scope: String): Single<List<Entity>>
}