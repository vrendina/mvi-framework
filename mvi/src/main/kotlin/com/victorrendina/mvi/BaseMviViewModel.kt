package com.victorrendina.mvi

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper
import android.support.annotation.RestrictTo
import android.util.Log
import com.victorrendina.rxqueue2.QueueSubject
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.Subject
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility

@SuppressLint("RxSubscribeOnError")
abstract class BaseMviViewModel<S : MviState, A : MviArgs>(
    initialState: S,
    protected val arguments: A? = null,
    private val debugMode: Boolean = false
) : ViewModel() {

    protected val tag: String by lazy { javaClass.simpleName }
    private val disposables = CompositeDisposable()

    private val stateStore: MviStateStore<S> = WorkerMviStateStore(initialState)
    private lateinit var mutableStateChecker: MutableStateChecker<S>

    // TODO Need to be able to send messages to multiple listeners
    private val messageQueue: Subject<Any> = QueueSubject.create<Any>().toSerialized()

    init {
        disposables.add(stateStore)
        if (debugMode) {
            mutableStateChecker = MutableStateChecker(initialState)

            Completable.fromRunnable {
                validateState()
            }.subscribeOn(Schedulers.computation()).subscribe().disposeOnClear()
        }
    }

    internal val state: S
        get() = stateStore.state

    /**
     * Call this to mutate the current state.
     */
    protected fun setState(reducer: S.() -> S) {
        if (debugMode) {
            // Must use `set` to ensure the validated state is the same as the actual state used in reducer
            // Do not use `get` since `getState` queue has lower priority and the validated state would be the state after reduced
            stateStore.set {
                val firstState = this.reducer()
                val secondState = this.reducer()

                if (firstState != secondState) throw IllegalArgumentException("Your reducer must be pure!")
                mutableStateChecker.onStateChanged(firstState)

                firstState
            }
        } else {
            stateStore.set(reducer)
        }
    }

    /**
     * Access the current ViewModel state. Takes a block of code that will be run after all current pending state
     * updates are processed. The `this` inside of the block is the state.
     */
    protected fun withState(block: (state: S) -> Unit) {
        stateStore.get(block)
    }

    /**
     * Enqueue a single message to be sent to the view.
     */
    protected fun sendMessage(message: Any) {
        messageQueue.onNext(message)
    }

    /**
     * Validates a number of properties on the state class. This cannot be called from the main thread because it does
     * a fair amount of reflection.
     */
    private fun validateState() {
        if (state::class.visibility != KVisibility.PUBLIC) {
            throw IllegalStateException("Your state class ${state::class.qualifiedName} must be public.")
        }
        state::class.assertImmutability()
    }

    /**
     * Helper to map an Single to an Async property on the state object.
     */
    fun <T> Single<T>.execute(
        stateReducer: S.(Async<T>) -> S
    ) = toObservable().execute({ it }, null, stateReducer)

    /**
     * Helper to map an Single to an Async property on the state object.
     * @param mapper A map converting the observable type to the desired AsyncData type.
     * @param stateReducer A reducer that is applied to the current state and should return the
     *                     new state. Because the state is the receiver and it likely a data
     *                     class, an implementation may look like: `{ copy(response = it) }`.
     */
    fun <T, V> Single<T>.execute(
        mapper: (T) -> V,
        stateReducer: S.(Async<V>) -> S
    ) = toObservable().execute(mapper, null, stateReducer)

    /**
     * Helper to map an observable to an Async property on the state object.
     */
    fun <T> Observable<T>.execute(
        stateReducer: S.(Async<T>) -> S
    ) = execute({ it }, null, stateReducer)

    /**
     * Execute an observable and wrap its progression with AsyncData reduced to the global state.
     *
     * @param mapper A map converting the observable type to the desired AsyncData type.
     * @param successMetaData A map that provides metadata to set on the Success result.
     *                        It allows data about the original Observable to be kept and accessed later. For example,
     *                        your mapper could map a network request to just the data your UI needs, but your base layers could
     *                        keep metadata about the request, like timing, for logging.
     * @param stateReducer A reducer that is applied to the current state and should return the
     *                     new state. Because the state is the receiver and it likely a data
     *                     class, an implementation may look like: `{ copy(response = it) }`.
     *
     *  @see Success.metadata
     */
    fun <T, V> Observable<T>.execute(
        mapper: (T) -> V,
        successMetaData: ((T) -> Any)? = null,
        stateReducer: S.(Async<V>) -> S
    ): Disposable {
        setState { stateReducer(Loading()) }

        return map {
            val success = Success(mapper(it))
            success.metadata = successMetaData?.invoke(it)
            success as Async<V>
        }
            .onErrorReturn { Fail(it) }
            .subscribe { asyncData -> setState { stateReducer(asyncData) } }
            .disposeOnClear()
    }

    /**
     * For ViewModels that want to subscribe to itself.
     */
    protected fun subscribe(subscriber: (S) -> Unit) =
        stateStore.observable.subscribeLifecycle(null, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun subscribe(owner: LifecycleOwner, subscriber: (S) -> Unit) =
        stateStore.observable.subscribeLifecycle(owner, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun subscribeMessages(owner: LifecycleOwner, subscriber: (Any) -> Unit) {
        MviMessageObserver(owner, messageQueue.observeOn(AndroidSchedulers.mainThread()), subscriber).disposeOnClear()
    }

    /**
     * Subscribe to state changes for only a single property.
     */
    protected fun <P> selectSubscribe(
        prop1: KProperty1<S, P>,
        subscriber: (P) -> Unit
    ) = selectSubscribeInternal(null, prop1, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <P> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, P>,
        subscriber: (P) -> Unit
    ) = selectSubscribeInternal(owner, prop1, subscriber)

    private fun <P> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, P>,
        subscriber: (P) -> Unit
    ) = stateStore.observable
        .map { MviTuple1(prop1.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner) { (p) -> subscriber(p) }

    /**
     * Subscribe to changes in an async property. There are optional parameters for onSuccess
     * and onFail which automatically unwrap the value or error.
     */
    protected fun <T> asyncSubscribe(
        asyncProp: KProperty1<S, Async<T>>,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = asyncSubscribeInternal(null, asyncProp, onFail, onSuccess)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <T> asyncSubscribe(
        owner: LifecycleOwner,
        asyncProp: KProperty1<S, Async<T>>,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = asyncSubscribeInternal(owner, asyncProp, onFail, onSuccess)

    private fun <T> asyncSubscribeInternal(
        owner: LifecycleOwner?,
        asyncProp: KProperty1<S, Async<T>>,
        onFail: ((Throwable) -> Unit)? = null,
        onSuccess: ((T) -> Unit)? = null
    ) = selectSubscribeInternal(owner, asyncProp) {
        if (onSuccess != null && it is Success) {
            onSuccess(it())
        } else if (onFail != null && it is Fail) {
            onFail(it.error)
        }
    }

    /**
     * Subscribe to state changes for two properties.
     */
    protected fun <P1, P2> selectSubscribe(
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        subscriber: (P1, P2) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <P1, P2> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        subscriber: (P1, P2) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, subscriber)

    private fun <P1, P2> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        subscriber: (P1, P2) -> Unit
    ) = stateStore.observable
        .map { MviTuple2(prop1.get(it), prop2.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner) { (p1, p2) -> subscriber(p1, p2) }

    /**
     * Subscribe to state changes for three properties.
     */
    protected fun <P1, P2, P3> selectSubscribe(
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        prop3: KProperty1<S, P3>,
        subscriber: (P1, P2, P3) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <P1, P2, P3> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        prop3: KProperty1<S, P3>,
        subscriber: (P1, P2, P3) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, subscriber)

    private fun <P1, P2, P3> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        prop3: KProperty1<S, P3>,
        subscriber: (P1, P2, P3) -> Unit
    ) = stateStore.observable
        .map { MviTuple3(prop1.get(it), prop2.get(it), prop3.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner) { (p1, p2, p3) -> subscriber(p1, p2, p3) }

    /**
     * Subscribe to state changes for four properties.
     */
    protected fun <P1, P2, P3, P4> selectSubscribe(
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        prop3: KProperty1<S, P3>,
        prop4: KProperty1<S, P4>,
        subscriber: (P1, P2, P3, P4) -> Unit
    ) = selectSubscribeInternal(null, prop1, prop2, prop3, prop4, subscriber)

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    fun <P1, P2, P3, P4> selectSubscribe(
        owner: LifecycleOwner,
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        prop3: KProperty1<S, P3>,
        prop4: KProperty1<S, P4>,
        subscriber: (P1, P2, P3, P4) -> Unit
    ) = selectSubscribeInternal(owner, prop1, prop2, prop3, prop4, subscriber)

    private fun <P1, P2, P3, P4> selectSubscribeInternal(
        owner: LifecycleOwner?,
        prop1: KProperty1<S, P1>,
        prop2: KProperty1<S, P2>,
        prop3: KProperty1<S, P3>,
        prop4: KProperty1<S, P4>,
        subscriber: (P1, P2, P3, P4) -> Unit
    ) = stateStore.observable
        .map { MviTuple4(prop1.get(it), prop2.get(it), prop3.get(it), prop4.get(it)) }
        .distinctUntilChanged()
        .subscribeLifecycle(owner) { (p1, p2, p3, p4) -> subscriber(p1, p2, p3, p4) }

    private fun <T> Observable<T>.subscribeLifecycle(
        lifecycleOwner: LifecycleOwner? = null,
        subscriber: (T) -> Unit
    ): Disposable {
        if (lifecycleOwner == null) {
            return observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber)
                .disposeOnClear()
        }

        val lifecycleAwareObserver = MviLifecycleAwareObserver(
            lifecycleOwner,
            alwaysDeliverLastValueWhenUnlocked = true,
            onNext = Consumer<T> { subscriber(it) }
        )
        return observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(lifecycleAwareObserver)
            .disposeOnClear()
    }

    /**
     * Output all state changes to logcat.
     */
    fun logStateChanges() {
        if (!debugMode) return
        subscribe { Log.d(tag, "New State: $it") }
    }

    protected fun Disposable.disposeOnClear(): Disposable {
        disposables.add(this)
        return this
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    override fun toString(): String = "$tag $state"
}