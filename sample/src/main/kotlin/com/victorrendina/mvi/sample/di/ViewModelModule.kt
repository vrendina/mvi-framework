package com.victorrendina.mvi.sample.di

import androidx.lifecycle.ViewModel
import com.victorrendina.mvi.BaseMviViewModel
import com.victorrendina.mvi.annotations.MviViewModelModule
import com.victorrendina.mvi.di.InjectableViewModelFactory
import com.victorrendina.mvi.di.ViewModelKey
import com.victorrendina.mvi.sample.counter.CounterViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.Multibinds

@Module
@MviViewModelModule
abstract class ViewModelModule {

    @Multibinds
    abstract fun viewModelFactories(): Map<Class<out ViewModel>, InjectableViewModelFactory<out BaseMviViewModel<*, *>, *, *>>

    @Binds
    @IntoMap
    @ViewModelKey(CounterViewModel::class)
    abstract fun counterFactory(factory: CounterViewModel.Factory): InjectableViewModelFactory<out BaseMviViewModel<*, *>, *, *>

}