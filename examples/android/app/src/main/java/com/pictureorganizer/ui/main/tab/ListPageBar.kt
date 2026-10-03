package com.pictureorganizer.ui.main.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pictureorganizer.R

@Composable
fun ListPageBar(
    pageIndex: Int,
    totalPages: Int,
    canGoPrev: Boolean,
    canGoNext: Boolean,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            onClick = onPrevClick,
            enabled = canGoPrev,
        ) {
            Text(stringResource(R.string.page_prev))
        }
        Text(
            text =
                stringResource(
                    R.string.page_indicator,
                    pageIndex + 1,
                    totalPages,
                ),
        )
        TextButton(
            onClick = onNextClick,
            enabled = canGoNext,
        ) {
            Text(stringResource(R.string.page_next))
        }
    }
}
