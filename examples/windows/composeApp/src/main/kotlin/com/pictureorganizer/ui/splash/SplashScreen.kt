package com.pictureorganizer.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SplashScreen(
    onNavigateNext: (tutorialCompleted: Boolean) -> Unit,
    resolveTutorialCompleted: suspend () -> Boolean,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        onNavigateNext(resolveTutorialCompleted())
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "图片整理",
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}
