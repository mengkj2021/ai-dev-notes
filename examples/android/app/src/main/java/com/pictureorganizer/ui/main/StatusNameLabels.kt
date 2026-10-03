package com.pictureorganizer.ui.main

import androidx.annotation.StringRes
import com.pictureorganizer.R
import com.pictureorganizer.model.ImageStatus

@StringRes
fun ImageStatus.statusNameRes(): Int =
    when (this) {
        ImageStatus.Pending -> R.string.status_name_pending
        ImageStatus.Confirmed -> R.string.status_name_confirmed
        ImageStatus.NoModify -> R.string.status_name_no_modify
    }
