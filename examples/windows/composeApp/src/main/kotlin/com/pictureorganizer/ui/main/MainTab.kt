package com.pictureorganizer.ui.main

import com.pictureorganizer.model.ImageStatus

enum class MainTab {
    Pending,
    Confirmed,
    NoModify,
    ;

    fun toStatus(): ImageStatus =
        when (this) {
            Pending -> ImageStatus.Pending
            Confirmed -> ImageStatus.Confirmed
            NoModify -> ImageStatus.NoModify
        }

    fun title(): String =
        when (this) {
            Pending -> "待归档"
            Confirmed -> "已归档"
            NoModify -> "回收站"
        }
}
