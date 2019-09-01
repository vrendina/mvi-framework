package com.victorrendina.mvi.sample.sliding

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.victorrendina.mvi.extensions.viewModel
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import com.victorrendina.mvi.withState
import kotlinx.android.synthetic.main.fragment_sample_sliding.*

class SampleSlidingFragment : BaseFragment() {

    private val viewModel: SampleSlidingViewModel by viewModel()

    private val bottomSheetBehavior by lazy(mode = LazyThreadSafetyMode.NONE) {
        BottomSheetBehavior.from(bottomSheet)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.selectSubscribe(SampleSlidingViewState::count) {
            countTextView.text = it.toString()
            sheetCountTextView.isGone = it % 2 == 0L
            sheetCountTextView.text = it.toString()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sample_sliding, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        slider.setDragView(draggableHeader)

        draggableHeaderTextView.setOnClickListener {
            Log.d("Test", "Clicked on draggable header")
        }

        countTextView.setOnClickListener {
            Log.d(
                "Test", "Count is ${withState(viewModel) { it.count }}"
            )
        }

        toggleBottomSheetButton.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                bottomSheetBehavior.isHideable = false
            } else {
                bottomSheetBehavior.isHideable = true
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }

        bottomSheetBehavior.setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(view: View, offset: Float) {
            }

            override fun onStateChanged(view: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_EXPANDED) {
                    slider.setDragEnabled(false)
                    Log.d(TAG, "Bottom sheet expanded, disabling the slider drag down")
                } else {
                    slider.setDragEnabled(true)
                }
            }
        })
    }

    companion object {
        private val TAG = SampleSlidingFragment::class.java.simpleName
    }
}