package com.victorrendina.mvi

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.support.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
class MviViewModelProviderFactory<VM : BaseMviViewModel<*, *>>(private val creator: () -> VM) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = creator.invoke() as T
}