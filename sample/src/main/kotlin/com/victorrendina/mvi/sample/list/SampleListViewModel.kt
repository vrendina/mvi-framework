package com.victorrendina.mvi.sample.list

import android.util.Log
import com.victorrendina.mvi.Async
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviState
import com.victorrendina.mvi.Success
import com.victorrendina.mvi.Uninitialized
import com.victorrendina.mvi.annotations.MviViewModel
import com.victorrendina.mvi.extensions.moveItem
import com.victorrendina.mvi.extensions.removeItem
import com.victorrendina.mvi.extensions.updateItems
import com.victorrendina.mvi.sample.data.Entity
import com.victorrendina.mvi.sample.data.EntityRepository
import com.victorrendina.mvi.sample.framework.BaseViewModel
import com.victorrendina.mvi.util.ValueLatch
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit

@Parcelize
data class SampleListArgs(val scope: String, val startingSelection: Int = -1) : MviArgs

data class SampleListViewState(
    // The loading state of the entities list
    val loading: Async<List<EntityListItem>> = Uninitialized,
    // The contents of the entity list
    val entities: List<EntityListItem> = emptyList()
) : MviState

@MviViewModel
class SampleListViewModel(
    initialState: SampleListViewState,
    arguments: SampleListArgs?,
    entityRepository: EntityRepository
) : BaseViewModel<SampleListViewState, SampleListArgs>(initialState, arguments) {

    // Keep references to the latches in a map so they can be used whenever an item needs updated
    private val toggleLatches = HashMap<String, ValueLatch<Boolean>>()
    private val sliderLatches = HashMap<String, ValueLatch<Int>>()

    /**
     * Converts [Entity] items from the database to [EntityListItem] items that can be displayed. Also initializes the value
     * latches for each item.
     */
    private val entityMapper: (List<Entity>) -> List<EntityListItem> = { list ->
        list.mapIndexed { index, entity ->
            EntityListItem(entity, selected = arguments?.startingSelection == index).also { createLatches(it) }
        }
    }

    init {
        Log.d(tag, "Initialized: $this")
        Log.d(tag, "Arguments $arguments")
//        logStateChanges()

        /*
         * Fetch the entities from the data source then map them to the proper entity type.
         */
        entityRepository.getEntities(arguments?.scope ?: "some scope")
            .map(entityMapper)
            .subscribeOn(Schedulers.io())
            .execute {
                copy(loading = it, entities = it() ?: entities)
            }

        /*
         * Example of sending a message to the view.
         */
        selectSubscribe(SampleListViewState::loading) {
            if (it is Success) {
                sendMessage("Loading successful")
            }
        }

        /*
         * Subscribe to updates to the entities list and log the selected items whenever the list changes.
         */
        selectSubscribe(SampleListViewState::entities) {
            logSelectedItems(it)
        }
    }

    private fun createLatches(item: EntityListItem) {
        val id = item.entity.id
        val toggleLatch = createToggleLatch(id, item.selected)
        val sliderLatch = createSliderLatch(id, item.slider)

        toggleLatches[id] = toggleLatch
        sliderLatches[id] = sliderLatch

        toggleLatch.disposeOnClear()
        sliderLatch.disposeOnClear()
    }

    private fun createToggleLatch(itemId: String, initialValue: Boolean): ValueLatch<Boolean> {
        return ValueLatch(initialValue, sendDelay = 500, restoreDelay = 2000,
            sender = { valueToSend ->
                sendToggleStateUpdate(itemId, valueToSend)
            },
            listener = { valueToShow ->
                setState {
                    copy(entities = entities.updateItems({ it.entity.id == itemId }) {
                        copy(selected = valueToShow)
                    })
                }
            }
        )
    }

    private fun createSliderLatch(itemId: String, initialValue: Int): ValueLatch<Int> {
        return ValueLatch(initialValue,
            sender = { valueToSend ->
                sendSliderStateUpdate(itemId, valueToSend)
            },
            listener = { valueToShow ->
                setState {
                    copy(entities = entities.updateItems({ it.entity.id == itemId }) {
                        copy(slider = valueToShow)
                    })
                }
            }
        )
    }

    private fun logSelectedItems(items: List<EntityListItem>) {
        items
            .filter { it.selected }
            .forEach {
                Log.d(tag, "Selected item: $it")
            }
    }

    fun moveEntity(fromIndex: Int, toIndex: Int) {
        setState {
            copy(entities = entities.moveItem(fromIndex, toIndex))
        }
    }

    fun removeEntity(index: Int) {
        setState {
            copy(entities = entities.removeItem(index))
        }
    }

    fun updateToggle(itemId: String, state: Boolean) {
        toggleLatches[itemId]?.setUiValue(state)
    }

    fun updateSlider(itemId: String, position: Int) {
        sliderLatches[itemId]?.setUiValue(position)
    }

    fun simulateStateUpdate() {
        // Delay the state update for one second
        withState { state ->
            Observable.timer(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    toggleLatches.forEach { (_, value) ->
                        value.setStateValue(true)
                    }
                    sliderLatches.forEach { (_, value) ->
                        value.setStateValue(50)
                    }
                }
        }
    }

    private fun sendToggleStateUpdate(itemId: String, value: Boolean) {
        Log.d(tag, "Sending toggle state $value for item $itemId")
    }

    private fun sendSliderStateUpdate(itemId: String, position: Int) {
        Log.d(tag, "Sending slider position $position for item $itemId")
    }
}