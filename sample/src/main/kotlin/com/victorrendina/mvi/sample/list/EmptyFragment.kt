package com.victorrendina.mvi.sample.list

import android.content.Context
import com.victorrendina.mvi.sample.SampleDep
import com.victorrendina.mvi.sample.framework.BaseFragment
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class EmptyFragment : BaseFragment() {

    @Inject
    lateinit var sampleDep: SampleDep

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}