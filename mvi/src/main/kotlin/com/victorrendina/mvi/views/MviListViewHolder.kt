package com.victorrendina.mvi.views

import android.content.Context
import android.content.res.Resources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView
import android.view.View
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
        lifecycleRegistry.markState(Lifecycle.State.INITIALIZED)
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
     * Called when this view holder is recycled.
     */
    protected open fun onRecycle() {
    }

    /**
     * Called when the RecyclerView is detached from the window and resources should be released.
     */
    protected open fun onDestroy() {
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
        boundItem = item
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