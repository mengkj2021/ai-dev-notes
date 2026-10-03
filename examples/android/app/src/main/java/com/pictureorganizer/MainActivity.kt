package com.pictureorganizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pictureorganizer.navigation.PictureOrganizerNavHost
import com.pictureorganizer.ui.theme.PictureOrganizerTheme
import com.pictureorganizer.util.log.AppLog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLog.d("Activity", "MainActivity onCreate savedInstanceState=${savedInstanceState != null}")
        enableEdgeToEdge()
        setContent {
            PictureOrganizerTheme {
                PictureOrganizerNavHost()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AppLog.d("Activity", "MainActivity onStart")
    }

    override fun onStop() {
        AppLog.d("Activity", "MainActivity onStop")
        super.onStop()
    }

    override fun onDestroy() {
        AppLog.d("Activity", "MainActivity onDestroy isFinishing=$isFinishing")
        super.onDestroy()
    }
}
