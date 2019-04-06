package com.victorrendina.mvi.sample.di

import android.content.Context
import android.content.res.Resources
import com.victorrendina.mvi.sample.MviSampleApplication
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    fun provideContext(application: MviSampleApplication): Context = application.applicationContext

    @Singleton
    @Provides
    fun provideResources(application: MviSampleApplication): Resources = application.resources

}