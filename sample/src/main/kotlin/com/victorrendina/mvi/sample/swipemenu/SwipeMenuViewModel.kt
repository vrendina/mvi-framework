package com.victorrendina.mvi.sample.swipemenu

import android.arch.lifecycle.ViewModel

/**
 * Created by Victor Rendina on 11/27/2018
 * Copyright 2018 Savant Systems LLC. All rights reserved.
 */

class SwipeMenuViewModel : ViewModel() {
    // Keep track of the set of open menu items so they can be restored on rotation
    val openItems = HashSet<Int>()
}