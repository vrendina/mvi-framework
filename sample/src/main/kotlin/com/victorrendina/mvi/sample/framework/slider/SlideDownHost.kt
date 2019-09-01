package com.victorrendina.mvi.sample.framework.slider

import android.view.View

interface SlideDownHost {

    fun setDragView(view: View)

    fun setDragEnabled(enabled: Boolean)

}