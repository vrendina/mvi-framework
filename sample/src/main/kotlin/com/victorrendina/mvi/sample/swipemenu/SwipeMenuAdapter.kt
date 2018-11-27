package com.victorrendina.mvi.sample.swipemenu

import android.arch.lifecycle.LifecycleOwner
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.victorrendina.mvi.extensions.inflate
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.views.MviListAdapter
import com.victorrendina.mvi.views.MviListViewHolder
import kotlinx.android.synthetic.main.list_item_swipeable.*

class SwipeMenuAdapter(lifecycleOwner: LifecycleOwner, private val openItems: HashSet<Int>) :
    MviListAdapter<Int>(lifecycleOwner) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MviListViewHolder<out Int> {
        return SwipeMenuViewHolder(parent.inflate(R.layout.list_item_swipeable))
    }

    inner class SwipeMenuViewHolder(itemView: View) : MviListViewHolder<Int>(itemView) {
        init {
            swipeMenu.setDragStateChangeListener { state ->
                Log.d("SwipeMenu", "Drag state is $state")
                when (state) {
                    SwipeRevealLayout.STATE_OPEN -> {
                        openItems.add(adapterPosition)
                    }
                    SwipeRevealLayout.STATE_CLOSED, SwipeRevealLayout.STATE_CLOSING -> {
                        openItems.remove(adapterPosition)
                    }
                }
            }

            itemTextView.setOnClickListener {
                swipeMenu.close(true)
            }

            menuButton.setOnClickListener {
                swipeMenu.close(true)
            }
        }

        override fun onBind(item: Int) {
            itemTextView.text = "Item #$item"
            if (openItems.contains(adapterPosition)) {
                swipeMenu.open(false)
            } else {
                swipeMenu.close(false)
            }
        }

    }
}