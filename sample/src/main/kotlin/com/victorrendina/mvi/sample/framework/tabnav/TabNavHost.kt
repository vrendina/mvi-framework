package com.victorrendina.mvi.sample.framework.tabnav

import androidx.annotation.IdRes
import com.victorrendina.mvi.sample.framework.nav.Screen

interface TabNavHost {

    /**
     * Navigate to the provided screen within the tab. Requests to start a new activity will be ignored
     * and this screen will be pushed into the tab's sub container.
     */
    fun pushScreen(screen: Screen)

    /**
     * Go backwards in the navigation stack or optionally navigate back to a specific fragment
     * by providing a tag.
     *
     * @return Boolean if back was successfully executed
     */
    fun goBack(backStackTag: String? = null): Boolean

    /**
     * Get the currently selected tab.
     */
    fun getCurrentTab(): TabRootItem

    /**
     * Programmatically change the currently selected tab using the id of the menu item.
     */
    fun setTab(@IdRes id: Int)

}
