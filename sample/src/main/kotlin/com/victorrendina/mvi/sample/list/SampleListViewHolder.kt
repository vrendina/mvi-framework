package com.victorrendina.mvi.sample.list

import android.view.View
import com.victorrendina.mvi.views.MviListViewHolder
import com.victorrendina.mvi.views.ThrottledSeekBarListener
import kotlinx.android.synthetic.main.list_item_entity.*

class SampleListViewHolder(
    itemView: View,
    private val viewModel: SampleListViewModel
) : MviListViewHolder<EntityListItem>(itemView) {

    override val moveEnabled: Boolean = true
    override val swipeDismissEnabled: Boolean = false

    // Delay slider updates by 500 ms
    private val seekBarChangeListener = ThrottledSeekBarListener(this, interval = 500) { position ->
        getCurrentId()?.also {
            viewModel.updateSlider(it, position)
        }
    }

    init {
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener)

        val clickListener = View.OnClickListener {
            checkBox.isChecked = !checkBox.isChecked
            getCurrentId()?.also { currentId ->
                viewModel.updateToggle(currentId, checkBox.isChecked)
            }
        }

        itemView.setOnClickListener(clickListener)
    }

    override fun onBind(item: EntityListItem) {
        entityId.text = item.entity.id
        entityName.text = item.entity.name
        checkBox.isChecked = item.selected
        seekBarValue.text = item.slider.toString()

        if (!seekBarChangeListener.interacting) {
            seekBar.progress = item.slider
        }
    }

    private fun getCurrentId(): String? {
        return boundItem?.entity?.id
    }
}