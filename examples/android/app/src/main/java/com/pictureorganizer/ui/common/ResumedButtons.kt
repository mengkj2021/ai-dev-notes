package com.pictureorganizer.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * TextButton that stays disabled until the host lifecycle is RESUMED
 * (Bug12 / Bug17：transition 中勿触发 pop / navigate).
 */
@Composable
fun ResumedTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val isResumed = rememberIsAtLeastResumed()
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && isResumed,
        content = content,
    )
}

/**
 * Button that stays disabled until the host lifecycle is RESUMED
 * (教程末页「开始使用 / 返回」等同族出口).
 */
@Composable
fun ResumedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val isResumed = rememberIsAtLeastResumed()
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && isResumed,
        content = content,
    )
}
