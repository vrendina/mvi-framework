package com.victorrendina.mvi.sample.resetables

import android.content.Context
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.victorrendina.mvi.extensions.fragmentViewModel
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import com.victorrendina.mvi.views.ThrottledSeekBarListener
import kotlinx.android.synthetic.main.fragment_resetable.*

class ResettableFragment : BaseFragment() {

    private val viewModel: ResettableViewModel by fragmentViewModel()

    private val seekBarChangeListener = ThrottledSeekBarListener(this, interval = 250) {
        viewModel.updateSliderFromUi(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectSubscribe(ResettableViewState::toggleState) {
            val onButtonBackground =
                if (it) requireContext().getColorCompat(R.color.toggleOn) else requireContext().getColorCompat(R.color.toggleOff)
            val offButtonBackground =
                if (it) requireContext().getColorCompat(R.color.toggleOff) else requireContext().getColorCompat(R.color.toggleOn)
            toggleOff.setBackgroundColor(offButtonBackground)
            toggleOn.setBackgroundColor(onButtonBackground)
        }

        viewModel.selectSubscribe(ResettableViewState::sliderPosition) {
            // Don't update the SeekBar value if the user is currently interacting
            if (!seekBarChangeListener.interacting) {
                seekBar.progress = it
            }
            continuousState.text = it.toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_resetable, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleOff.setOnClickListener {
            viewModel.updateToggleFromUi(false)
        }
        toggleOn.setOnClickListener {
            viewModel.updateToggleFromUi(true)
        }
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener)

        stateUpdate.setOnClickListener {
            viewModel.simulateStateUpdate()
        }
    }

    private fun Context.getColorCompat(@ColorRes id: Int): Int {
        return ContextCompat.getColor(this, id)
    }
}