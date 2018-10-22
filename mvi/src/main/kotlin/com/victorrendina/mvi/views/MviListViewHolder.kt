package com.victorrendina.mvi.views

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.extensions.LayoutContainer

abstract class MviListViewHolder<T>(
    override val containerView: View,
    lifecycleOwner: LifecycleOwner
) : RecyclerView.ViewHolder(containerView), LayoutContainer {

    var currentItem: T? = null
        private set

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                lifecycleOwner.lifecycle.removeObserver(this)
                destroy()
            }
        })
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
     * Called when the [LifecycleOwner] indicates it has been destroyed.
     */
    open fun onDestroy() {
    }

    fun bind(item: T) {
        if (currentItem != item) {
            onBind(item)
        }
        currentItem = item
    }

    fun attach() {
    }

    fun detach() {
    }

    fun recycle() {
        currentItem = null
        onRecycle()
    }

    fun destroy() {
        currentItem = null
        onDestroy()
    }

}