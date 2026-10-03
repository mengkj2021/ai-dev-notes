package com.pictureorganizer.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.pictureorganizer.R
import com.pictureorganizer.ui.common.LogScreenLifecycle
import com.pictureorganizer.ui.theme.PictureOrganizerTheme
import kotlinx.coroutines.delay

private const val SPLASH_DELAY_MS = 300L

@Composable
fun SplashScreen(
    onNavigateNext: (tutorialCompleted: Boolean) -> Unit,
    resolveTutorialCompleted: suspend () -> Boolean,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("Splash")
    LaunchedEffect(Unit) {
        delay(SPLASH_DELAY_MS)
        val completed = resolveTutorialCompleted()
        onNavigateNext(completed)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    PictureOrganizerTheme {
        SplashScreen(
            onNavigateNext = {},
            resolveTutorialCompleted = { true },
        )
    }
}
