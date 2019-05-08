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

    protected val tag: String by lazy { javaClass.simpleName }

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
     * Called when data is bound to the view holder.
     */
    protected abstract fun onBind(item: T)

    /**
     * Called when a non-empty change set is used to bind data to the view holder. Your adapter must
     * override getChangeSet to provide a set of changes for this method to be called.
     */
    @Deprecated("Check if the boundItem is equal to the item in the onBind method instead")
    protected open fun onBind(item: T, changeSet: Set<String>) {
    }

    /**
     * Called when this view holder is added to a RecycledViewPool and may be reused.
     */
    protected open fun onRecycle() {
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

    /**
     * Called when the view holder is no longer needed because it couldn't be added to a RecyclerView pool or the view has
     * been destroyed.
     */
    protected open fun onDestroy() {
    }

    /**
     * If the view holder is animating when an attempt is made to add it to the recycled view pool this method will be
     * invoked and the view holder should cancel any running animations so it can be reused.
     */
    open fun cancelAnimations() {
    }

    internal fun bind(item: T) {
        onBind(item)
        boundItem = item
    }

    @Deprecated("Internal method will be removed")
    internal fun bind(item: T, changeSet: Set<String>) {
        onBind(item, changeSet)
        boundItem = item
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
        onRecycle()
        boundItem = null
    }

    internal fun destroy() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED)
        boundItem = null
        onDestroy()
    }

    private fun isDestroyed() = lifecycle.currentState == Lifecycle.State.DESTROYED

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}