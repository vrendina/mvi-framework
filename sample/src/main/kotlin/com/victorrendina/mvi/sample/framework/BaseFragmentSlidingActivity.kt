package com.victorrendina.mvi.sample.framework

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.slider.SimpleSlideDownLayout
import com.victorrendina.mvi.sample.framework.slider.SlideDownHost
import kotlinx.android.synthetic.main.activity_fragment_sliding.*

open class BaseFragmentSlidingActivity : BaseFragmentActivity(), SlideDownHost {

    /**
     * Layout resource file that should be used for the activity. Every activity extending from this one must have
     * a layout that contains a view with an id of 'container' for the fragments.
     */
    override val layoutRes: Int = R.layout.activity_fragment_sliding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupSlidingLayout()

        if (container.parent is ViewGroup) {
            ViewCompat.setOnApplyWindowInsetsListener(container.parent as ViewGroup) { _, insets ->
                navigationBarBackground.updateLayoutParams {
                    height = insets.systemWindowInsetBottom
                }
                statusBarBackground.updateLayoutParams {
                    height = insets.systemWindowInsetTop
                }
                insets
            }
        }
    }

    override fun setDragView(view: View) {
        slidingDownLayout.setDragView(view)
        setDragEnabled(true)
    }

    override fun setDragEnabled(enabled: Boolean) {
        slidingDownLayout.isTouchEnabled = enabled
//        slidingDownLayout.setDragEnabled(enabled)
    }

    private fun setupSlidingLayout() {
        slidingDownLayout.addPanelSlideListener(object: SimpleSlideDownLayout.SimplePanelSlideListener() {
            override fun onPanelCollapsed(panel: View?) {
                finish()
                overridePendingTransition(0, R.anim.fade_out_fast)
            }
        })
//        slidingDownLayout.addSlideListener(object : SlideDownLayout.SlideListener {
//            override fun onSlide(view: View, offset: Float) {
//            }
//
//            override fun onStateChanged(view: View, state: SlideDownLayout.SlideState) {
//                if (state == SlideDownLayout.SlideState.COLLAPSED) {
//                    finish()
//                    overridePendingTransition(0, R.anim.fade_out_fast)
//                }
//            }
//        })
    }
}