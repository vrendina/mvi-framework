package com.victorrendina.mvi.extensions

import android.app.Activity
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.victorrendina.mvi.BaseMviViewModel
import com.victorrendina.mvi.LifecycleAwareLazy
import com.victorrendina.mvi.Mvi
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.MviView
import com.victorrendina.mvi.MviViewModelProviderFactory
import com.victorrendina.mvi.di.MviViewModelFactory
import com.victorrendina.mvi.di.MviViewModelFactoryOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

inline fun <T, reified VM : BaseMviViewModel<S, A>, reified S : MviState, reified A : MviArgs> T.viewModel(
    viewModelClass: KClass<VM> = VM::class
) where T : Fragment, T : MviView = LifecycleAwareLazy(this) {
    val arguments: A? = getFragmentArgs()
    val initialState: S = _initialStateProvider(arguments)

    val factory = MviViewModelProviderFactory {
        getViewModelFactory().create(viewModelClass.java, initialState, arguments)
    }
    ViewModelProviders.of(this, factory).get(viewModelClass.java)
}

inline fun <T, reified VM : BaseMviViewModel<S, A>, reified S : MviState, reified A : MviArgs> T.activityViewModel(
    viewModelClass: KClass<VM> = VM::class
) where T : Fragment, T : MviView = LifecycleAwareLazy(this) {
    val arguments: A? = getActivityArgs()
    val initialState: S = _initialStateProvider(arguments)

    val factory = MviViewModelProviderFactory {
        getViewModelFactory().create(viewModelClass.java, initialState, arguments)
    }
    ViewModelProviders.of(requireActivity(), factory).get(viewModelClass.java)
}

inline fun <T, reified VM : BaseMviViewModel<S, A>, reified S : MviState, reified A : MviArgs> T.viewModel(
    viewModelClass: KClass<VM> = VM::class
) where T : FragmentActivity, T : MviViewModelFactoryOwner, T : MviView =
    LifecycleAwareLazy(this) {
        val arguments: A? = getArgs()
        val initialState: S = _initialStateProvider(arguments)

        val factory = MviViewModelProviderFactory {
            viewModelFactory.create(viewModelClass.java, initialState, arguments)
        }
        ViewModelProviders.of(this, factory).get(viewModelClass.java)
    }

inline fun <T, reified VM : BaseMviViewModel<S, A>, reified S : MviState, reified A : MviArgs> T.parentViewModel(
        viewModelClass: KClass<VM> = VM::class
) where T : Fragment, T : MviView = LifecycleAwareLazy(this) {
    val arguments: A? = getParentFragmentArgs()
    val initialState: S = _initialStateProvider(arguments)

    val factory = MviViewModelProviderFactory {
        getViewModelFactory().create(viewModelClass.java, initialState, arguments)
    }
    ViewModelProviders.of(parentFragment!!, factory).get(viewModelClass.java)
}

inline fun <reified A : MviArgs> Fragment.getFragmentArgs(): A? = arguments?.get(Mvi.KEY_ARG) as? A

inline fun <reified A : MviArgs> Fragment.getActivityArgs(): A? {
    return requireActivity().intent?.extras?.get(Mvi.KEY_ARG) as? A
}

inline fun <reified A : MviArgs> Activity.getArgs(): A? {
    return intent?.extras?.get(Mvi.KEY_ARG) as? A
}

inline fun <reified A : MviArgs> Fragment.getParentFragmentArgs() : A? = parentFragment?.arguments?.get(Mvi.KEY_ARG) as? A

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
 * Property delegate to obtain Mvi arguments in fragments. To use define a property in your fragment like:
 *
 * `private val arguments: MyArgumentsType by args()`
 *
 * Where `MyArgumentsType` is the parcelable class implementing MviArgs and containing your arguments that were
 * provided to the fragment under the key [Mvi.KEY_ARG].
 */
fun <V : MviArgs> args() = object: ReadOnlyProperty<Fragment, V> {

    var value: V? = null

    override fun getValue(thisRef: Fragment, property: KProperty<*>): V {
        if (value == null) {
            val args = thisRef.arguments ?: throw IllegalArgumentException("There are no fragment arguments!")
            val argsUntyped = args.get(Mvi.KEY_ARG)
            argsUntyped ?: throw IllegalArgumentException("Could not find fragment arguments at key Mvi.KEY_ARG")
            @Suppress("UNCHECKED_CAST")
            value = argsUntyped as V
        }
        return value!!
    }
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
                argType, constructor.parameterTypes[0]
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