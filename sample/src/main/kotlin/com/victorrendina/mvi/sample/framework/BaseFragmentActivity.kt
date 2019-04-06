package com.victorrendina.mvi.sample.framework

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.nav.NavHost
import com.victorrendina.mvi.sample.framework.nav.Screen
import com.victorrendina.mvi.sample.framework.nav.getScreen
import kotlinx.android.synthetic.main.activity_fragment_base.*

open class BaseFragmentActivity : BaseActivity(), NavHost {

    /**
     * Layout resource file that should be used for the activity. Every activity extending from this one must have
     * a layout that contains a view with an id of 'container' for the fragments.
     */
    @LayoutRes
    protected open val layoutRes: Int = R.layout.activity_fragment_base

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)

        val screen = intent.getScreen()
        if (screen != null) {
            if (savedInstanceState == null) {
                createScreen(screen)
            }
        } else {
            // Warn about starting the activity without a screen
            Log.w(
                tag,
                "Started fragment activity without providing screen. Activity should be started by calling 'screen.startActivity(context)'"
            )
        }

    }

    open fun getBackgroundImage(): ImageView? {
        return backgroundImage
    }

    protected open fun createScreen(screen: Screen) {
        // Don't run the fragment enter/pop exit animations if this is the first screen, they will be run
        // by the activity.
        // TODO Change so doesn't get added to back stack either
        pushFragment(screen.copy(enterAnimation = 0, popExitAnimation = 0))
    }

    override fun animateActivityEnter() {
        val enterAnimation = intent.getScreen()?.enterAnimation ?: -1
        if (enterAnimation != -1) {
            overridePendingTransition(enterAnimation, R.anim.fade_out_slow)
        }
    }

    override fun animateActivityPopExit() {
        val popExitAnimation = intent.getScreen()?.popExitAnimation ?: -1
        if (popExitAnimation != -1) {
            overridePendingTransition(R.anim.fade_in, popExitAnimation)
        }
    }

    override fun pushScreen(screen: Screen) {
        if (screen.activity == null) {
            // Reuse this activity and push a new fragment
            pushFragment(screen)
        } else {
            // Start a new activity with the screen
            screen.startActivity(this)
        }
    }

    protected fun pushFragment(screen: Screen, fragment: Fragment = screen.createFragment()) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                screen.enterAnimation,
                screen.exitAnimation,
                screen.popEnterAnimation,
                screen.popExitAnimation
            )
            .replace(R.id.container, fragment)

        if (screen.addToBackStack) transaction.addToBackStack(screen.backStackTag)

        transaction.commit()
    }

    override fun onBackPressed() {
        // Check if any fragments want to consume this event before handling it
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is BaseFragment && fragment.isResumed && fragment.onBackPressed()) {
                return
            }
        }
        goBackInternal()
    }

    override fun goBack(backstackTag: String?) {
        if (backstackTag == null) {
            goBackInternal()
        } else {
            if (!supportFragmentManager.popBackStackImmediate(backstackTag, 0)) {
                // Close the activity if an invalid screen is provided
                finish()
            }
        }
    }

    /**
     * Go back to the previous screen or close this activity if it is the first screen in the stack.
     */
    private fun goBackInternal() {
        // TODO Don't add first fragment to the back stack
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    protected val tag: String = this::class.java.simpleName
}