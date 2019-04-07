package com.victorrendina.mvi.sample.framework.tabnav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import com.victorrendina.mvi.sample.framework.nav.Screen
import kotlinx.android.synthetic.main.fragment_tab_navigation.*

/**
 * When creating a screen with tab navigation you must override this fragment and provide a menu resource
 * and a screen bundle that will be used to initialize each tab based on the id specified in the menu resource.
 */
abstract class TabHostFragment : BaseFragment() {

    /**
     * Provide that menu resource for the bottom navigation bar.
     */
    @MenuRes
    protected abstract fun getMenu(): Int

    /**
     * Provide that screen that will be displayed in each tab based on item id.
     */
    protected abstract fun getScreenForItem(@IdRes itemId: Int): Screen

    private val adapter by lazy(LazyThreadSafetyMode.NONE) {
        TabNavAdapter(childFragmentManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Notify all child fragment tabs when their view is created which tab is currently selected
        childFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                if (f is BaseFragment) {
                    f.onTabSelected(getCurrentTab())
                }
            }
        }, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigation()
        setupScreens()
        setupViewPager()
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            setTab(item.itemId)
            true
        }
        bottomNavigation.inflateMenu(getMenu())
    }

    private fun setupViewPager() {
        tabNavViewPager.adapter = adapter
        tabNavViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                bottomNavigation.selectedItemId = adapter.data[position].id
                notifyTabSelected()
            }
        })
    }

    /**
     * Create the list of fragments for each menu item by associating each menu item id with a fragment class.
     */
    private fun setupScreens() {
        val data: ArrayList<TabRootItem> = ArrayList()
        val menu = bottomNavigation.menu
        for (index in 0 until menu.size()) {
            val menuItem = menu.getItem(index)
            data.add(TabRootItem(menuItem.itemId, getScreenForItem(menuItem.itemId)))
        }
        adapter.data = data
    }

    /**
     * Get the currently selected tab.
     */
    fun getCurrentTab(): TabRootItem {
        return adapter.data[tabNavViewPager.currentItem]
    }

    /**
     * Programmatically change the currently selected tab using the id of the menu item.
     */
    fun setTab(@IdRes id: Int) {
        tabNavViewPager.setItem(id)
    }

    /**
     * Notify all child fragments when a tab is selected by the user.
     */
    private fun notifyTabSelected() {
        val currentTab = getCurrentTab()
        childFragmentManager.fragments.forEach { fragment ->
            if (fragment is BaseFragment && fragment.view != null) {
                fragment.onTabSelected(currentTab)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        childFragmentManager.fragments.forEach { fragment ->
            if (fragment is TabRootFragment && fragment.isVisible) {
                return fragment.goBack()
            }
        }
        return false
    }
}