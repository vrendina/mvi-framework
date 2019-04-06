package com.victorrendina.mvi.sample.framework.dialog

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.extensions.args
import com.victorrendina.mvi.sample.R
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_message_dialog_simple.*

class SimpleMessageDialogFragment : BaseDialogFragment<SimpleMessageDialogFragment.Listener>(),
    View.OnClickListener {

    val arguments: SimpleMessageDialogArgs by args()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_message_dialog_simple, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments.title != null) {
            titleTextView.text = arguments.title
        } else titleTextView.visibility = View.GONE

        if (arguments.message != null) {
            messageTextView.text = arguments.message
        } else messageTextView.visibility = View.GONE

        if (arguments.subMessage != null) {
            subMessageTextView.text = arguments.subMessage
        } else subMessageTextView.visibility = View.GONE

        if (arguments.positive != null) {
            positiveButton.setOnClickListener(this)
            positiveButton.text = arguments.positive
        } else positiveButton.visibility = View.GONE

        if (arguments.negative != null) {
            negativeButton.setOnClickListener(this)
            negativeButton.text = arguments.negative
            negativeButton.paintFlags = negativeButton.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        } else negativeButton.visibility = View.GONE
    }

    override fun onClick(v: View) {
        dismiss()
        when (v.id) {
            R.id.positiveButton -> listener?.dialogPositiveClicked(arguments.extras)
            R.id.negativeButton -> listener?.dialogNegativeClicked(arguments.extras)
        }
    }

    interface Listener {
        fun dialogPositiveClicked(extras: MviArgs?) {}
        fun dialogNegativeClicked(extras: MviArgs?) {}
    }
}

@Parcelize
data class SimpleMessageDialogArgs(
    val title: String? = null,
    val message: String? = null,
    val subMessage: String? = null,
    val positive: String? = null,
    val negative: String? = null,
    val extras: MviArgs? = null
) : MviArgs