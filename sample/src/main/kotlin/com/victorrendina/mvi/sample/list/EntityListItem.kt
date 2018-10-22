package com.victorrendina.mvi.sample.list

import com.victorrendina.mvi.sample.data.Entity

data class EntityListItem(
    val entity: Entity,
    val selected: Boolean = false,
    val slider: Int = 25
) {
    override fun toString(): String {
        return "${entity.id.substring(0, 4)}... Selected: $selected Slider: $slider"
    }
}