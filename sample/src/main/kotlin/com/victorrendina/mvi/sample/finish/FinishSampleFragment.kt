package com.victorrendina.mvi.sample.finish

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.victorrendina.mvi.extensions.activityViewModel
import com.victorrendina.mvi.sample.R
import com.victorrendina.mvi.sample.framework.BaseFragment
import com.victorrendina.mvi.sample.framework.BaseFragmentActivity
import com.victorrendina.mvi.sample.framework.autofinish.AutoFinishViewModel
import com.victorrendina.mvi.sample.framework.autofinish.AutoFinishViewState
import com.victorrendina.mvi.sample.framework.autofinish.StringAutoFinishKey
import com.victorrendina.mvi.sample.framework.nav.screen
import com.victorrendina.mvi.withState
import kotlinx.android.synthetic.main.fragment_sample_finish.*

class FinishSampleFragment : BaseFragment() {

    private val autoFinishViewModel: AutoFinishViewModel by activityViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        autoFinish.registerKey(StringAutoFinishKey("sample"))

        autoFinishViewModel.selectSubscribe(AutoFinishViewState::enabled) {
            currentStatusTextView.text = "Auto finish enabled: $it"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sample_finish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pushScreenButton.setOnClickListener {
            nav.pushScreen(screen(FinishSampleFragment::class.java) {
                activity = BaseFragmentActivity::class.java
            })
        }

        finishAllButton.setOnClickListener {
            autoFinish.emitKey(StringAutoFinishKey("sample"))
        }

        toggleEnabled.setOnClickListener {
            autoFinish.setEnabled(withState(autoFinishViewModel) { !it.enabled })
        }
    }
}