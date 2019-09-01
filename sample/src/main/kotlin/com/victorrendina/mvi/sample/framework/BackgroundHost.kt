package com.victorrendina.mvi.sample.framework

import android.widget.ImageView

/**
 * Background images must be contained in the host activity so they can bleed under the navigation and status bars.
 * This interface is used to manage the background image that is displayed.
 */

interface BackgroundHost {

    /**
     * Get the image view that fills the entire screen including under the status and navigation bars.
     */
    fun getImageView(): ImageView

//
//    fun getStatusBarBackground(): ImageView
//
//    fun getNavigationBarBackground(): ImageView
}