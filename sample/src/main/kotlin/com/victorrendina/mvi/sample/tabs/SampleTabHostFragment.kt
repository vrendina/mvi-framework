package com.victorrendina.mvi.sample.tabs

import android.os.Bundle
import android.view.View
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.nav.Screen
import com.victorrendina.mvi.sample.framework.nav.screen
import com.victorrendina.mvi.sample.framework.tabnav.TabHostFragment
import kotlinx.android.synthetic.main.fragment_tab_navigation.*

class SampleTabHostFragment : TabHostFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tabNavViewPager.animateTabChanges = true
        tabNavViewPager.disableSwipe = false
    }

    override fun getMenu(): Int = R.menu.sample_tabs

    override fun getScreenForItem(itemId: Int): Screen {
        return when (itemId) {
            R.id.action_tab_one -> screen(SampleTabFragment::class.java) {
                arguments = SampleTabArgs("Root Tab One")
                backStackTag = "root"
            }
            R.id.action_tab_two -> screen(SampleTabFragment::class.java) {
                arguments = SampleTabArgs("Root Tab Two")
                backStackTag = "root"
            }
            else -> throw IllegalArgumentException("Unknown menu item")
        }
    }
}
