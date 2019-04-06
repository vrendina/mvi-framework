@file:Suppress("IllegalIdentifier")

package com.victorrendina.mvi.sample

import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import com.victorrendina.mvi.extensions.addArguments
import com.victorrendina.mvi.sample.counter.CounterActivity
import com.victorrendina.mvi.sample.counter.CounterArgs
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class CounterActivityTest {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(CounterActivity::class.java, true, false)

    @Before
    fun setIntent() {
        val intent = Intent().addArguments(CounterArgs(34))
        activityTestRule.launchActivity(intent)
    }

    @Test
    fun shared_Count_Increases() {
        val sharedTextView = onView(withId(R.id.sharedCounter))
        val increaseSharedButton = onView(withId(R.id.increaseShared))

        sharedTextView.check(matches(withText("34")))

        (0 until 20).forEach {
            increaseSharedButton.perform(click())
        }

        sharedTextView.check(matches(withText("54")))
    }

    @Test
    fun shared_Count_Decreases() {
        val decreaseFragmentButton = onView(withId(R.id.decreaseFragment))
        val coloredTextView = onView(withId(R.id.coloredCounter))

        coloredTextView.check(matches(withText("2")))

        (0 until 20).forEach {
            decreaseFragmentButton.perform(click())
        }

        coloredTextView.check(matches(withText("-18")))
    }
}
