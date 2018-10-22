package com.victorrendina.mvi.sample.di

import com.victorrendina.mvi.sample.data.EntityRepository
import com.victorrendina.mvi.sample.data.ProductionEntityRepository
import dagger.Binds
import dagger.Module

@Module
abstract class DataModule {

    @Binds
    abstract fun entitiyRepository(repository: ProductionEntityRepository): EntityRepository

}