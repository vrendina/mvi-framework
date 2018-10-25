package com.victorrendina.mvi.di

import com.victorrendina.mvi.BaseMviViewModel
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState

interface InjectableViewModelFactory<VM : BaseMviViewModel<S, A>, S : MviState, A : MviArgs> {
    fun create(initialState: S, arguments: A?): VM
}