package com.pictureorganizer.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pictureorganizer.util.log.AppLog

@Composable
fun LogScreenLifecycle(screen: String) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, screen) {
        AppLog.d("Lifecycle", "$screen composition ENTER")
        val observer =
            LifecycleEventObserver { _, event ->
                AppLog.d("Lifecycle", "$screen ${event.name}")
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            AppLog.d("Lifecycle", "$screen composition EXIT")
        }
    }
}
