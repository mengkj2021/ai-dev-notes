package com.pictureorganizer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.pictureorganizer.navigation.AppNavHost
import com.pictureorganizer.navigation.WindowChromeActions
import com.pictureorganizer.ui.theme.PictureOrganizerTheme
import com.pictureorganizer.util.log.AppLog

fun main() =
    application {
        AppLog.d("App", "PictureOrganizer Windows starting")
        val container = AppContainer()
        var chrome by remember { mutableStateOf(WindowChromeActions()) }
        val windowState =
            rememberWindowState(
                size = DpSize(1180.dp, 760.dp),
            )

        Window(
            onCloseRequest = ::exitApplication,
            title = "图片整理",
            state = windowState,
            onPreviewKeyEvent = { event ->
                if (event.type != KeyEventType.KeyDown) return@Window false
                when {
                    event.key == Key.Escape -> {
                        chrome.onEscape()
                        true
                    }
                    event.isCtrlPressed && event.key == Key.I -> {
                        chrome.onImport()
                        true
                    }
                    event.isCtrlPressed && event.key == Key.E -> {
                        chrome.onExport()
                        true
                    }
                    event.isCtrlPressed && event.key == Key.Comma -> {
                        chrome.onSettings()
                        true
                    }
                    event.isCtrlPressed && event.key == Key.A -> {
                        chrome.onSelectAll()
                        true
                    }
                    else -> false
                }
            },
        ) {
            window.minimumSize = java.awt.Dimension(800, 560)
            MenuBar {
                Menu("文件", mnemonic = 'F') {
                    Item(
                        "导入图片…",
                        onClick = chrome.onImport,
                        shortcut = androidx.compose.ui.input.key.KeyShortcut(Key.I, ctrl = true),
                        enabled = chrome.importEnabled,
                    )
                    Item(
                        "打包导出…",
                        onClick = chrome.onExport,
                        shortcut = androidx.compose.ui.input.key.KeyShortcut(Key.E, ctrl = true),
                        enabled = chrome.exportEnabled,
                    )
                    Separator()
                    Item(
                        "设置",
                        onClick = chrome.onSettings,
                        shortcut = androidx.compose.ui.input.key.KeyShortcut(Key.Comma, ctrl = true),
                        enabled = chrome.settingsEnabled,
                    )
                    Separator()
                    Item("退出", onClick = ::exitApplication)
                }
                Menu("编辑", mnemonic = 'E') {
                    Item(
                        "全选",
                        onClick = chrome.onSelectAll,
                        shortcut = androidx.compose.ui.input.key.KeyShortcut(Key.A, ctrl = true),
                        enabled = chrome.selectAllEnabled,
                    )
                }
            }
            PictureOrganizerTheme {
                AppNavHost(
                    container = container,
                    awtWindow = window,
                    onChromeChanged = { chrome = it },
                )
            }
        }
    }
