package com.victorrendina.mvi.sample.lists.single

import android.util.Log
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.annotations.MviViewModel
import com.victorrendina.mvi.di.InjectableViewModelFactory
import com.victorrendina.mvi.sample.framework.BaseViewModelArgs
import com.victorrendina.mvi.sample.lists.SampleListItem
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

data class SingleSelectionListViewState(
    val items: List<SampleListItem> = emptyList(),
    val selectedItemId: String? = null
) : MviState

@MviViewModel
class SingleSelectionListViewModel(
    initialState: SingleSelectionListViewState
) : BaseViewModelArgs<SingleSelectionListViewState, MviArgs>(initialState, null) {

    init {
        val initialData = (0 until 5000).map {
            SampleListItem(UUID.randomUUID().toString(), "Item $it", Random.nextInt())
        }

        // Seed with some initial data
        setState {
            copy(items = initialData)
        }

        selectSubscribe(SingleSelectionListViewState::selectedItemId) {
            Log.d(tag, "Selected item updated $it")
        }

        // Randomly generate some new numbers every 2 seconds and update the list
        Observable.interval(2, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .map { (0 until 5000).map { Random.nextInt() } }
            .subscribe { randomInts ->
                // Update each item in the list with a new random number so the diff util runs
                setState {
                    copy(items = items.mapIndexed { index: Int, item: SampleListItem ->
                        item.copy(randomInt = randomInts[index])
                    })
                }
            }.disposeOnClear()
    }

    fun updateSelection(item: SampleListItem) {
        setState {
            copy(selectedItemId = item.id)
        }
    }

    class Factory @Inject constructor() :
        InjectableViewModelFactory<SingleSelectionListViewModel, SingleSelectionListViewState, MviArgs> {
        override fun create(
            initialState: SingleSelectionListViewState,
            arguments: MviArgs?
        ): SingleSelectionListViewModel {
            return SingleSelectionListViewModel(initialState)
        }
    }
}