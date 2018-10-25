package com.victorrendina.mvi.views

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer

abstract class MviListViewHolder<T>(
    override val containerView: View
) : RecyclerView.ViewHolder(containerView), LayoutContainer, LifecycleOwner {

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
        lifecycleRegistry.markState(Lifecycle.State.INITIALIZED)
    }

    /**
     * Called when data is bound to the view holder. Your subclass is responsible for providing an implementation.
     */
    abstract fun onBind(item: T)

    /**
     * Called when this view holder is recycled.
     */
    open fun onRecycle() {
    }

    /**
     * Called when the RecyclerView is detached from the window and resources should be released.
     */
    open fun onDestroy() {
    }

    /**
     * When the view holder is bound and about to come on the screen.
     */
    open fun onStart() {
    }

    /**
     * Called when the view holder is detached from the screen. It may not be recycled.
     */
    open fun onStop() {
    }

    internal fun bind(item: T) {
        if (boundItem != item) {
            onBind(item)
        }
        boundItem = item
    }

    internal fun attach() {
        if (!isDestroyed()) {
            lifecycleRegistry.markState(Lifecycle.State.STARTED)
        }
    }

    internal fun detach() {
        if (!isDestroyed()) {
            lifecycleRegistry.markState(Lifecycle.State.CREATED)
        }
    }

    internal fun recycle() {
        boundItem = null
        onRecycle()
    }

    internal fun destroy() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
        boundItem = null
        onDestroy()
    }

    private fun isDestroyed() = lifecycle.currentState == Lifecycle.State.DESTROYED

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}