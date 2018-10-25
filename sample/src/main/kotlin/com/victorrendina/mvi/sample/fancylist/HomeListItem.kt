package com.victorrendina.mvi.sample.fancylist

sealed class HomeListItem

data class HomeServiceItem(
    val serviceId: String,
    val title: String
) : HomeListItem()

data class HomeHeaderItem(
    val roomId: String,
    val roomName: String
) : HomeListItem()

object HomeFooterItem : HomeListItem()