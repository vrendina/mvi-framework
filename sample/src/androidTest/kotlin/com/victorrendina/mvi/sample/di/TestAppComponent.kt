package com.victorrendina.mvi.sample.di

import com.victorrendina.mvi.sample.MviSampleApplication
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    ActivityModule::class,
    ViewModelModule::class,
    TestDataModule::class
])
interface TestAppComponent : AndroidInjector<MviSampleApplication> {
    @Component.Builder
    abstract class Builder: AndroidInjector.Builder<MviSampleApplication>()
}