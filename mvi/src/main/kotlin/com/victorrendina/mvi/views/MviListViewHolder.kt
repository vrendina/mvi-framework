package com.victorrendina.mvi.views

import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView
import com.victorrendina.mvi.MviView
import kotlinx.android.extensions.LayoutContainer

abstract class MviListViewHolder<T>(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer, LifecycleOwner, MviView {

    protected val context: Context = itemView.context
    protected val resources: Resources = context.resources

    @Suppress("LeakingThis")
    private val lifecycleRegistry = LifecycleRegistry(this)

    /**
     * The item that is currently bound to this view holder. This field will be non-null as long as the view holder is
     * in a bound state.
     */
    var boundItem: T? = null
        private set

    /**
     * If this view holder should be allowed to be moved. To enable support for moving your adapter must extend
     * [MviTouchableListAdapter].
     */
    open val moveEnabled: Boolean = false

    /**
     * If this view holder should be allowed to be swiped away to dismiss. To enable support for swipe dismiss your
     * adapter must extend [MviTouchableListAdapter].
     */
    open val swipeDismissEnabled: Boolean = false

    init {
        lifecycleRegistry.markState(Lifecycle.State.CREATED)
    }

    /**
     * Called when data is bound to the view holder. Your subclass is responsible for providing an implementation.
     */
    protected abstract fun onBind(item: T)

    /**
     * Called when a non-empty change set is used to bind data to the view holder. Your adapter must
     * override getChangeSet to provide a set of changes for this method to be called.
     */
    protected open fun onBind(item: T, changeSet: Set<String>) {
    }

    /**
     * Called when this view holder is recycled. It is impossible to tell if the view holder will be reused
     * again so any cleanup work should be done in this method, keeping in mind that the view could potentially
     * still be re-used.
     */
    protected open fun onRecycle() {
    }

    /**
     * Start any view model or RxJava subscriptions here and then clean them up in the corresponding [disposeSubscriptions]
     * method.
     */
    protected open fun startSubscriptions() {
    }

    /**
     * Dispose of any existing subscriptions. It is not necessary to do anything with view model subscriptions
     * but RxJava subscriptions should be disposed.
     */
    protected open fun disposeSubscriptions() {
    }

    /**
     * When the view holder is bound and about to come on the screen.
     */
    protected open fun onStart() {
    }

    /**
     * Called when the view holder is detached from the screen. It may not be recycled. It is possible for the view
     * to be detached and then reattached without recycling if it just moves slightly off screen.
     */
    protected open fun onStop() {
    }

    internal fun bind(item: T) {
        val initialState = lifecycleRegistry.currentState

        // Clean up any existing subscriptions by recycling this view when re-binding if it hasn't been recycled yet
        if (initialState != Lifecycle.State.DESTROYED) {
            recycle()
        }

        // Restore the lifecycle state of the view holder to the previous state before recycling
        if (initialState == Lifecycle.State.STARTED) {
            lifecycleRegistry.markState(Lifecycle.State.STARTED)
        } else {
            lifecycleRegistry.markState(Lifecycle.State.CREATED)
        }

        boundItem = item
        startSubscriptions()
        onBind(item)
    }

    internal fun bind(item: T, changeSet: Set<String>) {
        boundItem = item
        onBind(item, changeSet)
    }

    internal fun attach() {
        if (!isDestroyed()) {
            lifecycleRegistry.markState(Lifecycle.State.STARTED)
            onStart()
        }
    }

    internal fun detach() {
        if (!isDestroyed()) {
            lifecycleRegistry.markState(Lifecycle.State.CREATED)
            onStop()
        }
    }

    internal fun recycle() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
        disposeSubscriptions()
        onRecycle()
        boundItem = null
    }

    private fun isDestroyed() = lifecycle.currentState == Lifecycle.State.DESTROYED

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}