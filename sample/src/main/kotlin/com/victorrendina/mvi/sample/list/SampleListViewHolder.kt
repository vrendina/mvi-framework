package com.victorrendina.mvi.sample.list

import android.arch.lifecycle.LifecycleOwner
import android.view.View
import com.victorrendina.mvi.views.MviListViewHolder
import com.victorrendina.mvi.views.ThrottledSeekBarListener
import kotlinx.android.synthetic.main.list_item_entity.*

class SampleListViewHolder(
    itemView: View,
    lifecycleOwner: LifecycleOwner,
    private val viewModel: SampleListViewModel
) : MviListViewHolder<EntityListItem>(itemView, lifecycleOwner) {

    lateinit var currentId: String

    // Delay slider updates by 500 ms
    private val seekBarChangeListener = ThrottledSeekBarListener(lifecycleOwner, interval = 500) { position ->
        viewModel.updateSlider(currentId, position)
    }

    init {
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener)

        val clickListener = View.OnClickListener {
            checkBox.isChecked = !checkBox.isChecked
            viewModel.updateToggle(currentId, checkBox.isChecked)
        }

        itemView.setOnClickListener(clickListener)
    }

    override fun onBind(item: EntityListItem) {
        currentId = item.entity.id

        entityId.text = item.entity.id
        entityName.text = item.entity.name
        checkBox.isChecked = item.selected
        seekBarValue.text = item.slider.toString()

        if (!seekBarChangeListener.interacting) {
            seekBar.progress = item.slider
        }
    }
}