package com.pictureorganizer.ui.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TutorialScreen(
    fromSettings: Boolean,
    onFinished: () -> Unit,
    onSkipOrBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("欢迎使用图片整理", style = MaterialTheme.typography.headlineSmall)
        Text(
            "本应用在本地管理图片：导入复制到应用目录，按「待归档 / 已归档 / 回收站」分类，" +
                "可重命名、加标签，筛选后打包导出。全程不联网。",
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            "Windows 阶段①：已打通启动、教程、三 Tab 一览、设置、本地文件导入与详情基础操作。" +
                "筛选、标签库、重命名模板、zip 打包等见阶段②。",
            style = MaterialTheme.typography.bodyMedium,
        )
        Column(
            modifier = Modifier.align(Alignment.End),
            horizontalAlignment = Alignment.End,
        ) {
            Button(onClick = onFinished) {
                Text(if (fromSettings) "完成" else "开始使用")
            }
            if (fromSettings) {
                TextButton(onClick = onSkipOrBack) { Text("返回") }
            }
        }
    }
}
