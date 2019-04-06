package com.victorrendina.mvi.di

import androidx.lifecycle.ViewModel
import com.victorrendina.mvi.BaseMviViewModel
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import javax.inject.Inject

class MviViewModelFactory @Inject constructor(
    private val providers: Map<Class<out ViewModel>, @JvmSuppressWildcards InjectableViewModelFactory<out BaseMviViewModel<*, *>, *, *>>
) {

    fun <VM : BaseMviViewModel<S, A>, S : MviState, A : MviArgs> create(modelClass: Class<VM>, initialState: S, arguments: A?): VM {
        val creator = providers[modelClass]
                ?: providers.asIterable().firstOrNull { modelClass.isAssignableFrom(it.key) }?.value
                ?: throw IllegalArgumentException("Unknown ViewModel class $modelClass, did you annotate with @MviViewModel?")
        try {
            @Suppress("UNCHECKED_CAST")
            return (creator as InjectableViewModelFactory<VM, S, A>).create(initialState, arguments)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}