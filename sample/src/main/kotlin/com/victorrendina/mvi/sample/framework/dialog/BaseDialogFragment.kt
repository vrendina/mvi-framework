package com.victorrendina.mvi.sample.framework.dialog

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.CallSuper
import androidx.fragment.app.DialogFragment
import com.victorrendina.mvi.MviView
import com.victorrendina.mvi.sample.framework.BaseFragment


abstract class BaseDialogFragment<L> : DialogFragment(), MviView {

    /**
     * Override to customize the width of the dialog window.
     */
    open val dialogWidth: Int by lazy(mode = LazyThreadSafetyMode.NONE) {
        (256 * resources.displayMetrics.density).toInt()
    }

    /**
     * Override to customize the height of the dialog window. By default the height will be set to wrap content but
     * if you need to show scrolling content inside a dialog you can set a fixed height and should ask the designers
     * if they were sure about that...
     */
    open val dialogHeight: Int = ViewGroup.LayoutParams.WRAP_CONTENT

    /**
     * Override to customize the gravity of the dialog. By default the dialog will be centered in the window.
     */
    open val dialogGravity: Int = Gravity.CENTER

    @Suppress("UNCHECKED_CAST")
    protected val listener: L?
        get() = (parentFragment as? BaseFragment)?.getDialogListener(this) as? L

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        val params = dialog?.window?.attributes?.apply {
            width = dialogWidth
            height = dialogHeight
            gravity = dialogGravity
        }
        if (params != null) {
            dialog?.window?.attributes = params
        }
    }
}