package com.victorrendina.mvi.sample.di

import android.content.Context
import android.content.res.Resources
import com.victorrendina.mvi.sample.MviSampleApplication
import com.victorrendina.mvi.sample.framework.AppCoroutineDispatchers
import com.victorrendina.mvi.sample.framework.AppRxSchedulers
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.rx2.asCoroutineDispatcher
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    fun provideContext(application: MviSampleApplication): Context = application.applicationContext

    @Singleton
    @Provides
    fun provideResources(application: MviSampleApplication): Resources = application.resources

    @Singleton
    @Provides
    fun provideRxSchedulers(): AppRxSchedulers = AppRxSchedulers(
        io = Schedulers.io(),
        computation = Schedulers.computation(),
        main = AndroidSchedulers.mainThread()
    )

    @Singleton
    @Provides
    fun provideCoroutineDispatchers(schedulers: AppRxSchedulers) = AppCoroutineDispatchers(
        io = schedulers.io.asCoroutineDispatcher(),
        computation = schedulers.computation.asCoroutineDispatcher(),
        main = Dispatchers.Main
    )
}