package com.victorrendina.mvi.sample

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmokeTest {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(LaunchActivity::class.java, true, true)

    @Test
    fun launch_Activity_Started() {
    }
}