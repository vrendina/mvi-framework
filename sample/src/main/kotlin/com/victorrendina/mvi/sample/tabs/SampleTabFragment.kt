package com.victorrendina.mvi.sample.tabs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.victorrendina.mvi.MviArgs
import com.victorrendina.mvi.extensions.args
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import com.victorrendina.mvi.sample.framework.BaseFragmentActivity
import com.victorrendina.mvi.sample.framework.nav.screen
import com.victorrendina.mvi.sample.framework.tabnav.TabRootItem
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.fragment_sample_tab.*


class SampleTabFragment : BaseFragment() {

    private val arguments: SampleTabArgs by args()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sample_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        centerTextView.text = arguments.centerText

        pushActivityButton.setOnClickListener {
            nav.pushScreen(screen(SampleTabFragment::class.java) {
                arguments = SampleTabArgs("New activity pushed")
                activity = BaseFragmentActivity::class.java
            })
        }

        pushScreenButton.setOnClickListener {
            nav.pushScreen(screen(SampleTabFragment::class.java) {
                arguments = SampleTabArgs("Full screen fragment pushed")
            })
        }

        pushSubScreenButton.setOnClickListener {
            val depth = arguments.depth
            tabNav.pushScreen(screen(SampleTabFragment::class.java) {
                arguments = SampleTabArgs("Sub screen $depth", depth + 1)
                backStackTag = "${depth + 1}"
            })
        }

        goBackButton.setOnClickListener {
            try {
                if (!tabNav.goBack()) {
                    nav.goBack()
                }
            } catch (e: IllegalStateException) {
                nav.goBack()
            }
        }

        goBackToRoot.setOnClickListener {
            try {
                if (!tabNav.goBack("root")) {
                    nav.goBack()
                }
            } catch (e: IllegalStateException) {
                nav.goBack()
            }
        }
    }

    override fun onTabSelected(item: TabRootItem) {
        Log.d("SampleTab", "${arguments.centerText} -- Tab selected $item")
    }

}

@Parcelize
data class SampleTabArgs(
    val centerText: String,
    val depth: Int = 0
) : MviArgs