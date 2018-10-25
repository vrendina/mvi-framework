package com.victorrendina.mvi.extensions

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.victorrendina.mvi.BaseMviViewModel
import com.victorrendina.mvi.LifecycleAwareLazy
import com.victorrendina.mvi.Mvi
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.MviView
import com.victorrendina.mvi.MviViewModelProviderFactory
import com.victorrendina.mvi.di.MviViewModelFactory
import com.victorrendina.mvi.di.MviViewModelFactoryOwner
import kotlin.reflect.KClass

inline fun <T, reified VM : BaseMviViewModel<S, A>, reified S : MviState, reified A : MviArgs> T.viewModel(
    viewModelClass: KClass<VM> = VM::class
) where T: Fragment, T: MviView = LifecycleAwareLazy(this) {
    val arguments: A? = getFragmentArgs()
    val initialState: S = _initialStateProvider(arguments)

    val factory = MviViewModelProviderFactory {
        getViewModelFactory().create(viewModelClass.java, initialState, arguments)
    }
    ViewModelProviders.of(this, factory).get(viewModelClass.java)
}

inline fun <T, reified VM : BaseMviViewModel<S, A>, reified S : MviState, reified A : MviArgs> T.activityViewModel(
    viewModelClass: KClass<VM> = VM::class
) where T: Fragment, T: MviView = LifecycleAwareLazy(this) {
    val arguments: A? = getActivityArgs()
    val initialState: S = _initialStateProvider(arguments)

    val factory = MviViewModelProviderFactory {
        getViewModelFactory().create(viewModelClass.java, initialState, arguments)
    }
    ViewModelProviders.of(requireActivity(), factory).get(viewModelClass.java)
}

inline fun <T, reified VM : BaseMviViewModel<S, A>, reified S : MviState, reified A : MviArgs> T.viewModel(
    viewModelClass: KClass<VM> = VM::class
) where T: FragmentActivity, T: MviViewModelFactoryOwner, T: MviView =
    LifecycleAwareLazy(this) {
        val arguments: A? = getArgs()
        val initialState: S = _initialStateProvider(arguments)

        val factory = MviViewModelProviderFactory {
            viewModelFactory.create(viewModelClass.java, initialState, arguments)
        }
        ViewModelProviders.of(this, factory).get(viewModelClass.java)
    }

inline fun <reified A : MviArgs> Fragment.getFragmentArgs(): A? = arguments?.get(Mvi.KEY_ARG) as? A

inline fun <reified A : MviArgs> Fragment.getActivityArgs(): A? {
    return requireActivity().intent?.extras?.get(Mvi.KEY_ARG) as? A
}

inline fun <reified A : MviArgs> Activity.getArgs(): A? {
    return intent?.extras?.get(Mvi.KEY_ARG) as? A
}

fun Fragment.getViewModelFactory(): MviViewModelFactory {
    return (requireActivity() as? MviViewModelFactoryOwner)?.viewModelFactory
    ?: throw IllegalArgumentException("${this::class.simpleName} must be attached to an activity that is an ${MviViewModelFactoryOwner::class.simpleName}")
}

fun Intent.addArguments(args: MviArgs): Intent {
    putExtra(Mvi.KEY_ARG, args)
    return this
}

fun Fragment.addArguments(args: MviArgs): Fragment {
    val bundle = (arguments ?: Bundle()).apply {
        putParcelable(Mvi.KEY_ARG, args)
    }
    arguments = bundle
    return this
}

/**
 * For internal use only. Public for inline.
 *
 * Searches the view state class for a single argument constructor matching the type of [args]. If [args] is null, then
 * no arg constructor is invoked.
 *
 */
@Suppress("FunctionName")
inline fun <reified S : MviState, A : MviArgs> _initialStateProvider(args: A?): S {
    val stateClass = S::class.java
    val argsConstructor = args?.let {
        val argType = it::class.java

        stateClass.constructors.firstOrNull { constructor ->
            constructor.parameterTypes.size == 1 && isAssignableTo(
                constructor.parameterTypes[0],
                argType
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    return argsConstructor?.newInstance(args) as? S
        ?: try {
            stateClass.newInstance()
        } catch (@Suppress("TooGenericExceptionCaught") e: Throwable) {
            null
        }
        ?: throw IllegalStateException(
            "Attempt to auto create the Mvi state class ${stateClass.simpleName} has failed. It must have default values for every property or a " +
                "secondary constructor for ${args?.javaClass?.simpleName ?: "a fragment argument"}. "
        )
}