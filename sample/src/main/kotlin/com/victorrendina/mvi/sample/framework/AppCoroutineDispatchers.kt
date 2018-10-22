package com.victorrendina.mvi.sample.framework

import kotlinx.coroutines.experimental.CoroutineDispatcher

data class AppCoroutineDispatchers(
    val io: CoroutineDispatcher,
    val computation: CoroutineDispatcher,
    val main: CoroutineDispatcher
)