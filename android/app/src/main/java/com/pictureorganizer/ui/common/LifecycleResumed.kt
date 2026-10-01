package com.pictureorganizer.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

/** True when the current LocalLifecycleOwner is at least RESUMED (Bug12 / Bug17). */
@Composable
fun rememberIsAtLeastResumed(): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by lifecycleOwner.lifecycle.currentStateAsState()
    return state.isAtLeast(Lifecycle.State.RESUMED)
}
