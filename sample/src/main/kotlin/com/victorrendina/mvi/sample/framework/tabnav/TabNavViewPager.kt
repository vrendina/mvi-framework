package com.victorrendina.mvi.sample.framework.tabnav

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.view.MotionEvent
import androidx.annotation.IdRes
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class TabNavViewPager @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    /**
     * If swiping between pages should be disabled.
     */
    var disableSwipe = true

    /**
     * Animate changes when switching between tabs.
     */
    var animateTabChanges = true

    init {
        offscreenPageLimit = MAX_TABS - 1
        overScrollMode = ViewPager.OVER_SCROLL_NEVER
    }

    override fun setAdapter(adapter: PagerAdapter?) {
        if (adapter is TabNavAdapter) {
            super.setAdapter(adapter)
        } else {
            throw IllegalArgumentException("TabNavViewPager requires adapter of type TabNavAdapter")
        }
    }

    /**
     * Set the current tab using the id of a [MenuItem].
     */
    fun setItem(@IdRes id: Int) {
        val position = (adapter as TabNavAdapter).getItemPosition(id)
        if (position != -1) {
            currentItem = position
        }
    }

    /**
     * Set the current tab using the array index. This method should not be used.
     */
    override fun setCurrentItem(item: Int) {
        super.setCurrentItem(item, animateTabChanges)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (disableSwipe) false else super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if (disableSwipe) false else super.onTouchEvent(ev)
    }

    companion object {
        const val MAX_TABS = 5
    }

}