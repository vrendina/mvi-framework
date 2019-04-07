package com.victorrendina.mvi.sample.framework

import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.victorrendina.mvi.Mvi
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.MviView
import com.victorrendina.mvi.sample.framework.dialog.BaseDialogFragment
import com.victorrendina.mvi.sample.framework.nav.NavHost
import com.victorrendina.mvi.sample.framework.nav.getScreen
import com.victorrendina.mvi.sample.framework.tabnav.TabNavHost
import com.victorrendina.mvi.sample.framework.tabnav.TabRootItem

abstract class BaseFragment : Fragment(), MviView {

    /**
     * Navigation host used to navigate between screens within the application. See available methods
     * at [NavHost]. If you attempt to use navigation and your activity is not a [NavHost] this method
     * will crash the application.
     */
    protected val nav: NavHost by lazy(mode = LazyThreadSafetyMode.NONE) {
        activity as? NavHost
            ?: throw IllegalStateException("To use navigation in your fragment the host activity must implement 'NavHost'")
    }

    protected val tabNav: TabNavHost by lazy(mode = LazyThreadSafetyMode.NONE) {
        parentFragment as? TabNavHost
            ?: throw IllegalStateException("To use tab navigation your fragment must be hosted in a parent fragment that implements 'TabNavHost'")
    }

    protected val backgroundImage: ImageView?
        get() = (activity as? BaseFragmentActivity)?.getBackgroundImage()

    /**
     * Create and show a new dialog fragment that is a child fragment of the current fragment and attach
     * all the necessary arguments.
     */
    protected fun pushDialog(dialog: Class<out BaseDialogFragment<*>>, arguments: MviArgs? = null) {
        val dialogFragment = Class.forName(dialog.name).newInstance() as DialogFragment
        dialogFragment.arguments = Bundle().apply {
            putParcelable(Mvi.KEY_ARG, arguments)
        }
        dialogFragment.show(childFragmentManager, dialog.simpleName)
    }

    /**
     * Dialog fragments need to get an instance of their listener from their parent fragment whenever
     * they are re-created. To provide a listener for your dialog fragment, override this method in the dialog's
     * parent fragment and return the listener.
     */
    open fun getDialogListener(dialog: BaseDialogFragment<*>): Any? {
        return null
    }

    /**
     * Override this method to provide an action that should be taken when the hardware back button is pressed
     * inside this fragment. If you consume the back press you must return true from this method to prevent the
     * event from being propagated further.
     */
    open fun onBackPressed(): Boolean = false

    /**
     * Override this method if this fragment is hosted within a tab host fragment and you want to be notified when
     * a tab is selected. This method will also be called when the view for the tab is first created.
     */
    open fun onTabSelected(item: TabRootItem) {
    }

    /**
     * Workaround so child fragments don't disappear when their parent fragment is animated.
     */
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val rootParentFragment = this.rootParentFragment
        // Start suppressing child animations when the root parent is removed
        if (!enter && rootParentFragment?.isRemoving == true) {
            rootParentFragment.suppressChildAnimations = true
        }

        // Suppress the current animation if the root parent is animating
        if (rootParentFragment?.suppressChildAnimations == true) {
            val screen = rootParentFragment.arguments?.getScreen()
            if (screen != null) {
                val animation = if (enter) screen.enterAnimation else screen.exitAnimation
                val rootDuration =
                    if (animation != 0) AnimationUtils.loadAnimation(requireContext(), animation).duration else 0
                return AlphaAnimation(1f, 1f).apply {
                    duration = rootDuration
                }
            }
        }

        // Stop suppressing when the root parent enters the screen
        if (rootParentFragment == null && enter) {
            suppressChildAnimations = false
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    /**
     * Holds a reference to the root parent fragment. If a fragment is nested several levels deep this
     * property will always contain the top most fragment.
     */
    val rootParentFragment: BaseFragment? by lazy(mode = LazyThreadSafetyMode.NONE) {
        var currentParent = parentFragment
        while (true) {
            if (currentParent?.parentFragment == null) {
                break
            }
            currentParent = currentParent.parentFragment
        }
        currentParent as? BaseFragment
    }

    private var suppressChildAnimations = false

    @CallSuper
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SUPPRESS_CHILD_ANIMATIONS_KEY, suppressChildAnimations)
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        suppressChildAnimations = savedInstanceState?.getBoolean(SUPPRESS_CHILD_ANIMATIONS_KEY) ?: false
    }

    companion object {
        private const val SUPPRESS_CHILD_ANIMATIONS_KEY = "suppress_child_animations"
    }

}