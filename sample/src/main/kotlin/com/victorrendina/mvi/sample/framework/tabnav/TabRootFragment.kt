package com.victorrendina.mvi.sample.framework.tabnav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.extensions.args
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import com.victorrendina.mvi.sample.framework.nav.Screen
import kotlinx.android.parcel.Parcelize

/**
 * Fragment that serves as the root for each tab and allows navigation within individual tabs by providing
 * a separate back stack.
 */
class TabRootFragment : BaseFragment(), TabNavHost {

    private val arguments: TabRootArgs by args()

    private val tabHost: TabHostFragment
        get() = parentFragment as TabHostFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tab_root, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (childFragmentManager.fragments.isEmpty()) {
            pushScreen(arguments.initialScreen.copy(
                enterAnimation = 0,
                popExitAnimation = 0,
                addToBackStack = true
            ))
        }
    }

    override fun pushScreen(screen: Screen) {
        val transaction = childFragmentManager.beginTransaction()
            .setCustomAnimations(
                screen.enterAnimation,
                screen.exitAnimation,
                screen.popEnterAnimation,
                screen.popExitAnimation
            )
            .replace(R.id.subContainer, screen.createFragment())

        if (screen.addToBackStack) transaction.addToBackStack(screen.backStackTag)

        transaction.commit()
    }

    override fun goBack(backStackTag: String?): Boolean {
        if (backStackTag != null) {
            return childFragmentManager.popBackStackImmediate(backStackTag, 0)
        }

        if (childFragmentManager.backStackEntryCount > 1) {
            childFragmentManager.popBackStack()
            return true
        }

        return false
    }

    override fun getCurrentTab(): TabRootItem = tabHost.getCurrentTab()

    override fun setTab(@IdRes id: Int) {
        tabHost.setTab(id)
    }

    override fun onBackPressed(): Boolean = goBack(null)

    override fun onTabSelected(item: TabRootItem) {
        // Forward tab selection events to the children
        childFragmentManager.fragments.forEach { fragment ->
            if (fragment is BaseFragment && fragment.view != null) {
                fragment.onTabSelected(item)
            }
        }
    }

    @Parcelize
    class TabRootArgs(val initialScreen: Screen) : MviArgs
}