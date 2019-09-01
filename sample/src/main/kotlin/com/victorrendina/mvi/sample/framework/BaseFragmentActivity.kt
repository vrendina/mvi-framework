package com.victorrendina.mvi.sample.framework

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.util.Log
import android.widget.ImageView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.victorrendina.mvi.Mvi
import com.victorrendina.mvi.extensions.viewModel
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.autofinish.AutoFinishHost
import com.victorrendina.mvi.sample.framework.autofinish.AutoFinishKey
import com.victorrendina.mvi.sample.framework.autofinish.AutoFinishViewModel
import com.victorrendina.mvi.sample.framework.nav.NavHost
import com.victorrendina.mvi.sample.framework.nav.Screen
import com.victorrendina.mvi.sample.framework.nav.getScreen
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_fragment_base.*

open class BaseFragmentActivity : BaseActivity(), NavHost, BackgroundHost, AutoFinishHost {

    /**
     * Layout resource file that should be used for the activity. Every activity extending from this one must have
     * a layout that contains a view with an id of 'container' for the fragments.
     */
    @LayoutRes
    protected open val layoutRes: Int = R.layout.activity_fragment_base

    private val handler = Handler()
    private var lastPushedScreen: Screen? = null
    private val screenReset = Runnable { lastPushedScreen = null }

    private val autoFinishViewModel: AutoFinishViewModel by viewModel()
    private var autoFinishEventDisposable: Disposable? = null

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

        observeAutoFinishEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(screenReset)
        autoFinishEventDisposable?.dispose()
    }

    override fun getImageView(): ImageView = backgroundImage

    protected open fun createScreen(screen: Screen) {
        // Don't run the fragment enter/pop exit animations if this is the first screen, they will be run
        // by the activity.
        pushFragment(
            screen.copy(
                enterAnimation = 0,
                popExitAnimation = 0,
                addToBackStack = true
            )
        )
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
            if (canPushActivity(screen)) {
                screen.startActivity(this)
            }
        }
    }

    override fun pushScreenForResult(target: Fragment, screen: Screen, requestCode: Int) {
        if (screen.activity != null) {
            if (canPushActivity(screen)) {
                screen.startActivityForResult(target, requestCode)
            }
        }
    }

    /**
     * Prevent multiple activities from being opened with the same screen within 500 ms of each other. This will prevent
     * issues with double taps on buttons that push new screens.
     */
    private fun canPushActivity(screen: Screen): Boolean {
        val canPush = screen != lastPushedScreen

        if (lastPushedScreen == null) {
            lastPushedScreen = screen
            handler.postDelayed(screenReset, 500)
        }
        return canPush
    }

    override fun finishWithResult(result: Parcelable) {
        setResult(Activity.RESULT_OK, Intent().apply { putExtra(Mvi.KEY_ARG, result) })
        finish()
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

    override fun goBack(backStackTag: String?) {
        if (backStackTag == null) {
            goBackInternal()
        } else {
            if (!supportFragmentManager.popBackStackImmediate(backStackTag, 0)) {
                // Close the activity if an invalid screen is provided
                finish()
            }
        }
    }

    private fun observeAutoFinishEvents() {
        autoFinishEventDisposable = autoFinishViewModel.observeFinishEvents().subscribe {
            Log.d(tag, "Received auto finish event: $it")
            finish()
        }
    }

    override fun registerKey(key: AutoFinishKey) {
        autoFinishViewModel.registerKey(key)
    }

    override fun unregisterKey(key: AutoFinishKey) {
        autoFinishViewModel.unregisterKey(key)
    }

    override fun emitKey(key: AutoFinishKey) {
        autoFinishViewModel.emitKey(key)
    }

    override fun setEnabled(enabled: Boolean) {
        autoFinishViewModel.setEnabled(enabled)
    }

    /**
     * Go back to the previous screen or close this activity if it is the first screen in the stack.
     */
    private fun goBackInternal() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    protected val tag: String = this::class.java.simpleName
}