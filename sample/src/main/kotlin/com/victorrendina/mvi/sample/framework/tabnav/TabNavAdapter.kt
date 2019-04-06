package com.victorrendina.mvi.sample.framework.tabnav

import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.victorrendina.mvi.Mvi

class TabNavAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    var data: List<TabRootItem> = emptyList()
        set(value) {
            if (value.size <= TabNavViewPager.MAX_TABS) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun getItem(position: Int): Fragment {
        val rootFragment = TabRootFragment()
        rootFragment.arguments = Bundle().apply {
            putParcelable(
                Mvi.KEY_ARG, TabRootFragment.TabRootArgs(
                    initialScreen = data[position].screen
                )
            )
            putInt(TAB_ID, data[position].id)
        }
        return rootFragment
    }

    override fun getCount(): Int = data.size

    override fun getItemPosition(`object`: Any): Int {
        (`object` as? Fragment)?.also { fragment ->
            val tabId = fragment.arguments?.getInt(TAB_ID) ?: -1
            val position = data.indexOfFirst { tabId == it.id }
            if (position != -1) {
                return position
            }
        }
        return PagerAdapter.POSITION_NONE
    }

    fun getItemPosition(item: MenuItem) = data.indexOfFirst { it.id == item.itemId }

    fun getItemPosition(@IdRes id: Int) = data.indexOfFirst { it.id == id }

    companion object {
        private const val TAB_ID = "tab_id"
    }
}