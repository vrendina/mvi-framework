package com.victorrendina.mvi.sample.fancylist

import android.util.Log
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.annotations.MviViewModel
import com.victorrendina.mvi.sample.framework.BaseViewModel

data class HomeListViewState(
    val items: List<HomeListItem> = emptyList(),
    val editMode: Boolean = false
) : MviState

@MviViewModel
class HomeListViewModel(
    initialState: HomeListViewState
) : BaseViewModel<HomeListViewState, MviArgs>(initialState, null) {

    init {
        Log.d(tag, "Initialized: $this")
        logStateChanges()

        val sampleItems = listOf(
            HomeHeaderItem("test_room", "Test Room"),
            HomeServiceItem("Service 1", "Service 1"),
            HomeServiceItem("Service 2", "Service 2"),
            HomeServiceItem("Service 3", "Service 3"),
            HomeFooterItem
        )

        setState {
            copy(items = sampleItems)
        }
    }

    fun setEditMode(active: Boolean) {
        setState {
            copy(editMode = active)
        }
    }
}