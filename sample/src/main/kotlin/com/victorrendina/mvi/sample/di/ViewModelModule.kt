package com.victorrendina.mvi.sample.di

import android.arch.lifecycle.ViewModel
import com.victorrendina.mvi.BaseMviViewModel
import com.victorrendina.mvi.annotations.MviViewModelModule
import com.victorrendina.mvi.di.InjectableViewModelFactory
import com.victorrendina.mvi.di.ViewModelKey
import com.victorrendina.mvi.sample.counter.CounterViewModel
import com.victorrendina.mvi.sample.counter.CounterViewModelFactory
import com.victorrendina.mvi.sample.list.SampleListViewModel
import com.victorrendina.mvi.sample.list.SampleListViewModelFactory
import com.victorrendina.mvi.sample.resetables.ResetableViewModelFactory
import com.victorrendina.mvi.sample.resetables.ResettableViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds

@Module
@MviViewModelModule
abstract class ViewModelModule {

    @Multibinds
    abstract fun viewModelFactories(): Map<Class<out ViewModel>, InjectableViewModelFactory<out BaseMviViewModel<*, *>, *, *>>

    // TODO These should be automatically generated from the @MviViewModel annotation
    @Binds
    @IntoMap
    @ViewModelKey(CounterViewModel::class)
    abstract fun counterFactory(factory: CounterViewModelFactory): InjectableViewModelFactory<out BaseMviViewModel<*, *>, *, *>

    @Binds
    @IntoMap
    @ViewModelKey(SampleListViewModel::class)
    abstract fun sampleListFactory(factory: SampleListViewModelFactory): InjectableViewModelFactory<out BaseMviViewModel<*, *>, *, *>

    @Binds
    @IntoMap
    @ViewModelKey(ResettableViewModel::class)
    abstract fun resetableFactory(factory: ResetableViewModelFactory): InjectableViewModelFactory<out BaseMviViewModel<*, *>, *, *>
}