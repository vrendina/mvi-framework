package com.victorrendina.mvi.sample.di

import com.victorrendina.mvi.sample.data.EntityRepository
import com.victorrendina.mvi.sample.data.TestEntityRepository
import dagger.Binds
import dagger.Module

@Module
abstract class TestDataModule: DataModule() {

    @Binds
    abstract fun entitiyRepository(repository: TestEntityRepository): EntityRepository

}