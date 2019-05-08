package com.victorrendina.mvi.sample.lists.multi

import android.util.Log
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.annotations.MviViewModel
import com.victorrendina.mvi.di.InjectableViewModelFactory
import com.victorrendina.mvi.extensions.moveItem
import com.victorrendina.mvi.extensions.removeItem
import com.victorrendina.mvi.extensions.updateItems
import com.victorrendina.mvi.sample.framework.BaseViewModel
import com.victorrendina.mvi.sample.lists.SampleListItem
import com.victorrendina.mvi.views.SelectableListItem
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.Collections
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

data class MultiSelectionListViewState(
    val items: List<SelectableListItem<SampleListItem>> = emptyList()
) : MviState

@MviViewModel
class MultiSelectionListViewModel(
    initialState: MultiSelectionListViewState
) : BaseViewModel<MultiSelectionListViewState, MviArgs>(initialState, null) {

    init {
        val initialData = (0 until 1000).map {
            SelectableListItem(SampleListItem(UUID.randomUUID().toString(), "Item $it", Random.nextInt()), false)
        }

        // Seed with some initial data
        setState {
            copy(items = initialData)
        }
    }

    fun toggleSelection(item: SelectableListItem<SampleListItem>) {
        setState {
            copy(items = items.updateItems({ it.data.id == item.data.id }, {
                copy(selected = !selected)
            }))
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        setState {
            copy(items = items.moveItem(fromIndex, toIndex))
        }
    }

    fun removeItem(index: Int) {
        setState {
            copy(items = items.removeItem(index))
        }
    }

    class Factory @Inject constructor() :
        InjectableViewModelFactory<MultiSelectionListViewModel, MultiSelectionListViewState, MviArgs> {
        override fun create(
            initialState: MultiSelectionListViewState,
            arguments: MviArgs?
        ): MultiSelectionListViewModel {
            return MultiSelectionListViewModel(initialState)
        }
    }
}