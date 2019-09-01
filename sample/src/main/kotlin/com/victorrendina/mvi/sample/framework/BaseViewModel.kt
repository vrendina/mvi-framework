package com.victorrendina.mvi.sample.framework

import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState

/**
 * Base view model class when arguments are not required. If you need to pass arguments to your view model
 * use [BaseViewModelArgs].
 */

abstract class BaseViewModel<S : MviState>(initialState: S) : BaseViewModelArgs<S, MviArgs>(initialState, null)