package com.victorrendina.mvi.views

import android.arch.lifecycle.LifecycleOwner
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.victorrendina.mvi.extensions.moveItem
import com.victorrendina.mvi.extensions.removeItem

abstract class MviTouchableListAdapter<T, H : MviListViewHolder<T>>(lifecycleOwner: LifecycleOwner) :
    MviListAdapter<T, H>(lifecycleOwner) {

    protected abstract val dragEnabled: Boolean
    protected open val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN

    protected abstract val swipeDismissEnabled: Boolean
    protected open val swipeDismissFlags = ItemTouchHelper.START or ItemTouchHelper.END

    private val touchHelper by lazy(mode = LazyThreadSafetyMode.NONE) {
        ItemTouchHelper(
            ItemTouchHelperCallback(
                this,
                dragEnabled,
                swipeDismissEnabled,
                dragFlags,
                swipeDismissFlags
            )
        )
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        touchHelper.attachToRecyclerView(null)
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        syncMoveToViewModel(fromIndex, toIndex)
        updateDataImmediate(data.moveItem(fromIndex, toIndex), false)
        notifyItemMoved(fromIndex, toIndex)
    }

    fun removeItem(index: Int) {
        syncSwipeDismissToViewModel(index)
        updateDataImmediate(data.removeItem(index))
        notifyItemRemoved(index)
    }

    abstract fun syncMoveToViewModel(fromIndex: Int, toIndex: Int)

    abstract fun syncSwipeDismissToViewModel(index: Int)

}