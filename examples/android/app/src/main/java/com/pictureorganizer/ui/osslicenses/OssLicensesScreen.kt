package com.pictureorganizer.ui.osslicenses

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.pictureorganizer.R
import com.pictureorganizer.ui.common.BackNavIconButton
import com.pictureorganizer.ui.common.LogScreenLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OssLicensesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("OssLicenses")
    val context = LocalContext.current
    val libraries by produceLibraries {
        context.resources
            .openRawResource(R.raw.aboutlibraries)
            .bufferedReader()
            .use { it.readText() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.oss_licenses_title)) },
                navigationIcon = {
                    BackNavIconButton(
                        onBack = onBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                },
            )
        },
    ) { innerPadding ->
        LibrariesContainer(
            libraries = libraries,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }
}
