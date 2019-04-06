package com.victorrendina.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
class MviViewModelProviderFactory<VM : BaseMviViewModel<*, *>>(private val creator: () -> VM) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = creator.invoke() as T
}