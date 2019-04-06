package com.victorrendina.mvi.sample.framework.tabnav

import androidx.annotation.IdRes
import com.victorrendina.mvi.sample.framework.nav.Screen

interface TabNavHost {

    /**
     * Navigate to the provided screen within the tab. It is not possible to
     * start a new activity.
     */
    fun pushScreen(screen: Screen)

    /**
     * Go backwards in the navigation stack or optionally navigate back to a specific fragment
     * by providing a tag.
     *
     * @return Boolean if back was successfully executed
     */
    fun goBack(backstackTag: String? = null): Boolean

    /**
     * Get the currently selected tab.
     */
    fun getCurrentTab(): TabRootItem

    /**
     * Programmatically change the currently selected tab using the id of the menu item.
     */
    fun setTab(@IdRes id: Int)

}
