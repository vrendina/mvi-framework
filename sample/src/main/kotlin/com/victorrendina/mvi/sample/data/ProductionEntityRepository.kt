package com.victorrendina.mvi.sample.data

import io.reactivex.Single
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductionEntityRepository @Inject constructor(
    private val localSource: EntityLocalDataSource
) : EntityRepository {

    override fun getEntities(scope: String): Single<List<Entity>> {
        return localSource.getEntities(scope)
    }
}