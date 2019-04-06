package com.victorrendina.mvi.sample.tabs

import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.nav.Screen
import com.victorrendina.mvi.sample.framework.nav.screen
import com.victorrendina.mvi.sample.framework.tabnav.TabHostFragment

class SampleTabHostFragment: TabHostFragment() {

    override fun getMenu(): Int = R.menu.sample_tabs

    override fun getScreenForItem(itemId: Int): Screen {
        return when (itemId) {
            R.id.action_tab_one -> screen(SampleTabFragment::class.java) {
                arguments = SampleTabArgs("Root Tab One")
            }
            R.id.action_tab_two -> screen(SampleTabFragment::class.java) {
                arguments = SampleTabArgs("Root Tab Two")
            }
            else -> throw IllegalArgumentException("Unknown menu item")
        }
    }
}
