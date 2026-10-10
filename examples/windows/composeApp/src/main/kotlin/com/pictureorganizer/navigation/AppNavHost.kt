package com.pictureorganizer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pictureorganizer.AppContainer
import com.pictureorganizer.ui.defaulttags.DefaultTagsScreen
import com.pictureorganizer.ui.exportmanage.ExportManageScreen
import com.pictureorganizer.ui.exportzip.ExportZipScreen
import com.pictureorganizer.ui.filter.FilterScreen
import com.pictureorganizer.ui.imagedetail.ImageDetailScreen
import com.pictureorganizer.ui.importimages.ImportScreen
import com.pictureorganizer.ui.main.MainScreen
import com.pictureorganizer.ui.main.MainViewModel
import com.pictureorganizer.ui.renametemplate.RenameTemplateEditScreen
import com.pictureorganizer.ui.renametemplate.RenameTemplateManageScreen
import com.pictureorganizer.ui.settings.SettingsScreen
import com.pictureorganizer.ui.splash.SplashScreen
import com.pictureorganizer.ui.tagmanage.TagManageScreen
import com.pictureorganizer.ui.tutorial.TutorialScreen
import java.awt.Window
import kotlinx.coroutines.launch

data class WindowChromeActions(
    val onImport: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onSettings: () -> Unit = {},
    val onSelectAll: () -> Unit = {},
    val onEscape: () -> Unit = {},
    val importEnabled: Boolean = false,
    val exportEnabled: Boolean = false,
    val settingsEnabled: Boolean = false,
    val selectAllEnabled: Boolean = false,
)

private data class NavState(
    val route: String = Routes.SPLASH,
    val detailId: String? = null,
    val tutorialFromSettings: Boolean = false,
    val renameEditId: String? = null,
)

@Composable
fun AppNavHost(
    container: AppContainer,
    awtWindow: Window,
    onChromeChanged: (WindowChromeActions) -> Unit,
    modifier: Modifier = Modifier,
) {
    var nav by remember { mutableStateOf(NavState()) }
    val scope = rememberCoroutineScope()
    val mainVm =
        remember {
            MainViewModel(container.images, container.renameTemplates, container.prefs)
        }
    DisposableEffect(Unit) {
        onDispose { mainVm.dispose() }
    }

    fun goMain() {
        nav = NavState(route = Routes.MAIN)
    }

    fun goBack() {
        nav =
            when (nav.route) {
                Routes.MAIN -> {
                    mainVm.clearSelection()
                    nav
                }
                Routes.SETTINGS,
                Routes.IMPORT_IMAGES,
                Routes.IMAGE_DETAIL,
                Routes.FILTER,
                Routes.EXPORT_ZIP,
                -> NavState(route = Routes.MAIN)
                Routes.EXPORT_MANAGE,
                Routes.TAG_MANAGE,
                Routes.DEFAULT_TAGS,
                Routes.RENAME_TEMPLATE_MANAGE,
                -> NavState(route = Routes.SETTINGS)
                Routes.RENAME_TEMPLATE_EDIT -> NavState(route = Routes.RENAME_TEMPLATE_MANAGE)
                Routes.TUTORIAL ->
                    NavState(
                        route = if (nav.tutorialFromSettings) Routes.SETTINGS else Routes.MAIN,
                        tutorialFromSettings = nav.tutorialFromSettings,
                    )
                else -> nav
            }
    }

    LaunchedEffect(nav.route) {
        val onMain = nav.route == Routes.MAIN
        onChromeChanged(
            WindowChromeActions(
                onImport = {
                    if (onMain || nav.route == Routes.IMPORT_IMAGES) {
                        nav = NavState(route = Routes.IMPORT_IMAGES)
                    }
                },
                onExport = {
                    if (onMain) nav = NavState(route = Routes.EXPORT_ZIP)
                },
                onSettings = {
                    if (onMain || nav.route == Routes.SETTINGS) {
                        nav = NavState(route = Routes.SETTINGS)
                    }
                },
                onSelectAll = {
                    if (onMain) mainVm.selectAllVisible()
                },
                onEscape = {
                    if (onMain) {
                        mainVm.clearSelection()
                    } else if (nav.route != Routes.SPLASH) {
                        goBack()
                    }
                },
                importEnabled = onMain || nav.route == Routes.IMPORT_IMAGES,
                exportEnabled = onMain,
                settingsEnabled = onMain || nav.route == Routes.SETTINGS,
                selectAllEnabled = onMain,
            ),
        )
    }

    when (nav.route) {
        Routes.SPLASH ->
            SplashScreen(
                modifier = modifier,
                resolveTutorialCompleted = { container.prefs.isTutorialCompleted() },
                onNavigateNext = { completed ->
                    nav =
                        if (completed) {
                            NavState(route = Routes.MAIN)
                        } else {
                            NavState(route = Routes.TUTORIAL, tutorialFromSettings = false)
                        }
                },
            )

        Routes.TUTORIAL ->
            TutorialScreen(
                modifier = modifier,
                fromSettings = nav.tutorialFromSettings,
                onFinished = {
                    scope.launch {
                        container.prefs.setTutorialCompleted(true)
                        goMain()
                    }
                },
                onSkipOrBack = {
                    nav = NavState(route = if (nav.tutorialFromSettings) Routes.SETTINGS else Routes.MAIN)
                },
            )

        Routes.MAIN ->
            MainScreen(
                modifier = modifier,
                viewModel = mainVm,
                files = container.files,
                onNavigateToImport = { nav = NavState(route = Routes.IMPORT_IMAGES) },
                onNavigateToDetail = { id -> nav = NavState(route = Routes.IMAGE_DETAIL, detailId = id) },
                onNavigateToSettings = { nav = NavState(route = Routes.SETTINGS) },
                onNavigateToFilter = { nav = NavState(route = Routes.FILTER) },
                onNavigateToExportZip = { nav = NavState(route = Routes.EXPORT_ZIP) },
            )

        Routes.SETTINGS ->
            SettingsScreen(
                modifier = modifier,
                prefs = container.prefs,
                onBack = ::goMain,
                onOpenTutorial = { nav = NavState(route = Routes.TUTORIAL, tutorialFromSettings = true) },
                onOpenTagManage = { nav = NavState(route = Routes.TAG_MANAGE) },
                onOpenDefaultTags = { nav = NavState(route = Routes.DEFAULT_TAGS) },
                onOpenRenameTemplates = { nav = NavState(route = Routes.RENAME_TEMPLATE_MANAGE) },
                onOpenExportManage = { nav = NavState(route = Routes.EXPORT_MANAGE) },
            )

        Routes.IMPORT_IMAGES ->
            ImportScreen(
                modifier = modifier,
                images = container.images,
                tags = container.tags,
                prefs = container.prefs,
                files = container.files,
                awtWindow = awtWindow,
                onBack = ::goMain,
            )

        Routes.IMAGE_DETAIL ->
            ImageDetailScreen(
                modifier = modifier,
                imageId = nav.detailId.orEmpty(),
                images = container.images,
                tags = container.tags,
                renameTemplates = container.renameTemplates,
                files = container.files,
                onBack = ::goMain,
            )

        Routes.FILTER ->
            FilterScreen(
                modifier = modifier,
                initial = mainVm.uiState.value.filter,
                availableTags = emptyList(),
                tagFlow = container.tags.observeTags(),
                onBack = ::goMain,
                onApply = { criteria ->
                    mainVm.applyFilter(criteria)
                    goMain()
                },
            )

        Routes.EXPORT_ZIP ->
            ExportZipScreen(
                modifier = modifier,
                images = container.images,
                files = container.files,
                awtWindow = awtWindow,
                onBack = ::goMain,
            )

        Routes.EXPORT_MANAGE ->
            ExportManageScreen(
                modifier = modifier,
                files = container.files,
                onBack = { nav = NavState(route = Routes.SETTINGS) },
            )

        Routes.TAG_MANAGE ->
            TagManageScreen(
                modifier = modifier,
                tags = container.tags,
                images = container.images,
                prefs = container.prefs,
                onBack = { nav = NavState(route = Routes.SETTINGS) },
            )

        Routes.DEFAULT_TAGS ->
            DefaultTagsScreen(
                modifier = modifier,
                tags = container.tags,
                prefs = container.prefs,
                onBack = { nav = NavState(route = Routes.SETTINGS) },
            )

        Routes.RENAME_TEMPLATE_MANAGE ->
            RenameTemplateManageScreen(
                modifier = modifier,
                repo = container.renameTemplates,
                onBack = { nav = NavState(route = Routes.SETTINGS) },
                onEdit = { id -> nav = NavState(route = Routes.RENAME_TEMPLATE_EDIT, renameEditId = id) },
            )

        Routes.RENAME_TEMPLATE_EDIT ->
            RenameTemplateEditScreen(
                modifier = modifier,
                repo = container.renameTemplates,
                templateId = nav.renameEditId,
                onBack = { nav = NavState(route = Routes.RENAME_TEMPLATE_MANAGE) },
            )
    }
}
