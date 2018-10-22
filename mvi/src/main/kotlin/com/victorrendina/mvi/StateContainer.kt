package com.victorrendina.mvi

/**
 * Accesses ViewModel state from a single ViewModel synchronously and returns the result of the block.
 */
fun <VM : BaseMviViewModel<S, *>, S : MviState, R> withState(viewModel: VM, block: (S) -> R) =
    block(viewModel.state)

/**
 * Accesses ViewModel state from two ViewModels synchronously and returns the result of the block.
 */
fun <VM1 : BaseMviViewModel<S1, *>, S1 : MviState,
    VM2 : BaseMviViewModel<S2, *>, S2 : MviState,
    R> withState(
    viewModel1: VM1,
    viewModel2: VM2,
    block: (S1, S2) -> R
) = block(viewModel1.state, viewModel2.state)