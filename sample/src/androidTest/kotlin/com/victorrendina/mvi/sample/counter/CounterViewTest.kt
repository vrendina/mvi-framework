@file:Suppress("IllegalIdentifier")

package com.victorrendina.mvi.sample.counter

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.victorrendina.mvi.sample.BaseAndroidTest
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragmentActivity
import com.victorrendina.mvi.sample.framework.nav.screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CounterViewTest: BaseAndroidTest() {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(BaseFragmentActivity::class.java, true, false)

    @Before
    fun setIntent() {
        val screen = screen(CounterFragment::class.java) {
            activity = BaseFragmentActivity::class.java
            arguments = CounterArgs(34)
            enterAnimation = 0
        }

        val activity = activityTestRule.launchActivity(null)
        activity.pushScreen(screen)
    }

    @Test
    fun shared_Count_Increases() {
        val sharedTextView = onView(withId(R.id.sharedCounter))
        val increaseSharedButton = onView(withId(R.id.increaseShared))
        val decreaseSharedButton = onView(withId(R.id.decreaseShared))

        sharedTextView.check(matches(withText("34")))

        for (it in 0 until 5) {
            increaseSharedButton.perform(click())
            decreaseSharedButton.perform(click())
        }

        sharedTextView.check(matches(withText("34")))
    }

    @Test
    fun shared_Count_Decreases() {
        val decreaseFragmentButton = onView(withId(R.id.decreaseFragment))
        val coloredTextView = onView(withId(R.id.coloredCounter))

        coloredTextView.check(matches(withText("34")))

        for (it in 0 until 10) {
            decreaseFragmentButton.perform(click())
        }

        coloredTextView.check(matches(withText("24")))
    }
}
