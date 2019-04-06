package com.victorrendina.mvi.views

data class SelectableListItem<T>(
    val data: T,
    val selected: Boolean
) {

    /**
     * Return a new instance of the [SelectableListItem] with the data updated using the provided
     * reducer function.
     */
    fun updateData(reducer: T.() -> T): SelectableListItem<T> {
        return copy(data = data.reducer())
    }

}