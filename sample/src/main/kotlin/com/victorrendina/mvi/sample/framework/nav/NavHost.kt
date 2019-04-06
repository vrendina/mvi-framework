package com.victorrendina.mvi.sample.framework.nav

interface NavHost {

    /**
     * Navigate to the provided screen.
     */
    fun pushScreen(screen: Screen)

    /**
     * Go backwards in the navigation stack or optionally navigate back to a specific fragment
     * by providing a tag.
     */
    fun goBack(backstackTag: String? = null)

    /**
     * Close the activity. Equivalent to calling the finish method directly on the activity. Typically
     * should not be overridden and is just here as a convenience for fragments.
     */
    fun finish()

}