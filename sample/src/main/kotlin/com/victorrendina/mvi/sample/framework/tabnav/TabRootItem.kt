package com.victorrendina.mvi.sample.framework.tabnav

import androidx.annotation.IdRes
import com.victorrendina.mvi.sample.framework.nav.Screen

data class TabRootItem(
    @IdRes val id: Int, // id of tab item from menu resource file
    val screen: Screen // screen that should be displayed in the tab
)