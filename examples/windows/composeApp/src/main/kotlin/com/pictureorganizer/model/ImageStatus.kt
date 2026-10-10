package com.pictureorganizer.model

enum class ImageStatus {
    Pending,
    Confirmed,
    NoModify,
    ;

    fun displayName(): String =
        when (this) {
            Pending -> "待归档"
            Confirmed -> "已归档"
            NoModify -> "回收站"
        }
}
