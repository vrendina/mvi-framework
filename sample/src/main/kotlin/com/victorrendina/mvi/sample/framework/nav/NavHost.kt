package com.victorrendina.mvi.sample.framework.nav

import android.os.Parcelable
import androidx.fragment.app.Fragment

interface NavHost {

    /**
     * Navigate to the provided screen.
     */
    fun pushScreen(screen: Screen)

    /**
     * Navigate to the provided screen in a new activity to receive data back as a result in onActivityResult.
     */
    fun pushScreenForResult(target: Fragment, screen: Screen, requestCode: Int = 0)

    /**
     * Go backwards in the navigation stack or optionally navigate back to a specific fragment
     * by providing a tag.
     */
    fun goBack(backStackTag: String? = null)

    /**
     * Close the activity. Equivalent to calling the finish method directly on the activity. Typically
     * should not be overridden and is just here as a convenience for fragments.
     */
    fun finish()

    /**
     * Close the activity and return a result to the target fragment if started with [pushScreenForResult].
     */
    fun finishWithResult(result: Parcelable)
}