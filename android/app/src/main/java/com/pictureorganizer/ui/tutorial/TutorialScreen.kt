package com.pictureorganizer.ui.tutorial

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pictureorganizer.R
import com.pictureorganizer.ui.common.LogScreenLifecycle
import com.pictureorganizer.ui.common.ResumedButton
import com.pictureorganizer.ui.common.ResumedTextButton
import com.pictureorganizer.ui.theme.PictureOrganizerTheme
import kotlinx.coroutines.launch

private data class TutorialPage(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
)

private val tutorialPages =
    listOf(
        TutorialPage(R.string.tutorial_page1_title, R.string.tutorial_page1_body),
        TutorialPage(R.string.tutorial_page2_title, R.string.tutorial_page2_body),
        TutorialPage(R.string.tutorial_page3_title, R.string.tutorial_page3_body),
    )

@Composable
fun TutorialScreen(
    onFinished: () -> Unit,
    fromSettings: Boolean = false,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("Tutorial")
    val pagerState = rememberPagerState(pageCount = { tutorialPages.size })
    val scope = rememberCoroutineScope()
    val lastIndex = tutorialPages.lastIndex
    val exitLabelRes =
        if (fromSettings) R.string.tutorial_close else R.string.tutorial_skip
    val finishLabelRes =
        if (fromSettings) R.string.tutorial_return else R.string.tutorial_start

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                ResumedTextButton(onClick = onFinished) {
                    Text(stringResource(exitLabelRes))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) { page ->
                val item = tutorialPages[page]
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(item.titleRes),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    TutorialBodyText(
                        raw = stringResource(item.bodyRes),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(tutorialPages.size) { index ->
                    val selected = pagerState.currentPage == index
                    Surface(
                        modifier =
                            Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (selected) 10.dp else 8.dp),
                        shape = CircleShape,
                        color =
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                    ) {}
                }
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (pagerState.currentPage < lastIndex) {
                    Button(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                    ) {
                        Text(stringResource(R.string.tutorial_next))
                    }
                } else {
                    ResumedButton(onClick = onFinished) {
                        Text(stringResource(finishLabelRes))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TutorialScreenPreview() {
    PictureOrganizerTheme {
        TutorialScreen(onFinished = {})
    }
}
