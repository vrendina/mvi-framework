package com.victorrendina.mvi.views

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import android.widget.SeekBar
import com.victorrendina.mvi.util.ValueThrottler
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class ThrottledSeekBarListener(
    lifecycleOwner: LifecycleOwner,
    interval: Long = 250,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    listener: (Int) -> Unit
) : ValueThrottler<Int>(interval, unit, AndroidSchedulers.mainThread(), listener),
    SeekBar.OnSeekBarChangeListener {

    /**
     * If the user is currently interacting with the [SeekBar]. When the user is interacting, state updates that set
     * the value of the [SeekBar] should be ignored so the bar does not jump to another value while the user is still
     * sliding.
     */
    var interacting = false
        private set

    init {
        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                dispose()
                lifecycleOwner.lifecycle.removeObserver(this)
            }
        })
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            emitValue(progress)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        interacting = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        emitValue(seekBar.progress)
        interacting = false
    }
}