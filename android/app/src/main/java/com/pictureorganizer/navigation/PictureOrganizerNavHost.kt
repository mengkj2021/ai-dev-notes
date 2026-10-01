package com.pictureorganizer.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pictureorganizer.PictureOrganizerApplication
import com.pictureorganizer.model.TagFilterCriteria
import com.pictureorganizer.ui.defaulttags.DefaultTagsScreen
import com.pictureorganizer.ui.exportmanage.ExportManageScreen
import com.pictureorganizer.ui.exportmanage.ExportManageViewModel
import com.pictureorganizer.ui.exportzip.ExportZipScreen
import com.pictureorganizer.ui.exportzip.ExportZipUiEvent
import com.pictureorganizer.ui.exportzip.ExportZipViewModel
import com.pictureorganizer.ui.filter.FilterScreen
import com.pictureorganizer.ui.filter.FilterViewModel
import com.pictureorganizer.ui.imagedetail.ImageDetailScreen
import com.pictureorganizer.ui.importimages.ImportScreen
import com.pictureorganizer.ui.main.MainScreen
import com.pictureorganizer.ui.main.MainTab
import com.pictureorganizer.ui.main.MainUiEvent
import com.pictureorganizer.ui.main.MainViewModel
import com.pictureorganizer.ui.osslicenses.OssLicensesScreen
import com.pictureorganizer.ui.renametemplate.RenameTemplateEditScreen
import com.pictureorganizer.ui.renametemplate.RenameTemplateEditViewModel
import com.pictureorganizer.ui.renametemplate.RenameTemplateManageScreen
import com.pictureorganizer.ui.renametemplate.RenameTemplateManageUiEvent
import com.pictureorganizer.ui.renametemplate.RenameTemplateManageViewModel
import com.pictureorganizer.ui.settings.SettingsScreen
import com.pictureorganizer.ui.splash.SplashScreen
import com.pictureorganizer.ui.tagmanage.TagManageScreen
import com.pictureorganizer.ui.tutorial.TutorialScreen
import com.pictureorganizer.util.log.AppLog
import kotlinx.coroutines.launch

private const val RENAME_TEMPLATE_SAVED_KEY = "rename_template_saved"

private const val DETAIL_VISIBLE_SIBLING_COUNT_KEY = "detail_visible_sibling_count"

private fun NavController.backQueueBrief(): String =
    currentBackStack.value.joinToString(separator = ">") { entry ->
        entry.destination.route ?: "?"
    }

private fun NavController.popRouteIfOnTop(
    expectedRoutePrefix: String,
    reason: String,
): Boolean {
    val currentEntry = currentBackStackEntry
    val current = currentEntry?.destination?.route
    val previous = previousBackStackEntry?.destination?.route
    val before = backQueueBrief()
    val sizeBefore = currentBackStack.value.size
    val lifecycleState = currentEntry?.lifecycle?.currentState
    AppLog.d(
        "Nav",
        "popRoute request reason=$reason expect=$expectedRoutePrefix current=$current " +
            "previous=$previous size=$sizeBefore lifecycle=$lifecycleState queue=$before",
    )
    if (current == null || !current.startsWith(expectedRoutePrefix)) {
        AppLog.w(
            "Nav",
            "popRoute skipped: route mismatch (reason=$reason expect=$expectedRoutePrefix " +
                "current=$current size=$sizeBefore queue=$before)",
        )
        return false
    }

    if (lifecycleState != null && !lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) {
        AppLog.w(
            "Nav",
            "popRoute skipped: lifecycle not RESUMED (reason=$reason lifecycle=$lifecycleState " +
                "current=$current size=$sizeBefore queue=$before)",
        )
        return false
    }

    if (previous == null || sizeBefore <= 1) {
        AppLog.w(
            "Nav",
            "popRoute skipped: would empty stack (reason=$reason current=$current " +
                "size=$sizeBefore queue=$before)",
        )
        return false
    }
    val ok = popBackStack()
    AppLog.d(
        "Nav",
        "popRoute done reason=$reason ok=$ok size=${currentBackStack.value.size} " +
            "queue=${backQueueBrief()}",
    )
    return ok
}

private fun NavController.popFilterIfOnTop(reason: String): Boolean = popRouteIfOnTop("filter", reason)

@Composable
fun PictureOrganizerNavHost() {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as PictureOrganizerApplication
    val repository = app.imageRepository
    val tagRepository = app.tagRepository
    val userPreferences = app.userPreferencesRepository
    val renameTemplateRepository = app.renameTemplateRepository
    val scope = rememberCoroutineScope()

    DisposableEffect(navController) {
        val listener =
            NavController.OnDestinationChangedListener { controller, destination, args ->
                AppLog.d(
                    "Nav",
                    "destination=${destination.route} size=${controller.currentBackStack.value.size} " +
                        "queue=${controller.backQueueBrief()} args=$args",
                )
            }
        navController.addOnDestinationChangedListener(listener)
        onDispose { navController.removeOnDestinationChangedListener(listener) }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                resolveTutorialCompleted = { userPreferences.isTutorialCompleted() },
                onNavigateNext = { tutorialCompleted ->
                    val destination =
                        if (tutorialCompleted) {
                            Routes.MAIN
                        } else {
                            Routes.tutorial(fromSettings = false)
                        }
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
            )
        }
        composable(
            route = Routes.TUTORIAL,
            arguments =
                listOf(
                    navArgument("fromSettings") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                ),
        ) { entry ->
            val fromSettings = entry.arguments?.getBoolean("fromSettings") ?: false
            TutorialScreen(
                fromSettings = fromSettings,
                onFinished =
                    dropUnlessResumed {
                        scope.launch {
                            if (!fromSettings) {
                                userPreferences.setTutorialCompleted(true)
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                navController.popRouteIfOnTop(
                                    "tutorial",
                                    "tutorialBackFromSettings",
                                )
                            }
                        }
                    },
            )
        }
        composable(Routes.MAIN) { entry ->
            val mainViewModel: MainViewModel =
                viewModel(
                    factory =
                        MainViewModel.Factory(
                            repository,
                            tagRepository,
                            renameTemplateRepository,
                            userPreferences,
                        ),
                )
            val filterApplied by entry.savedStateHandle
                .getStateFlow(FilterResultKeys.APPLIED, false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(filterApplied) {
                if (!filterApplied) return@LaunchedEffect
                val tagsRaw = entry.savedStateHandle.get<String>(FilterResultKeys.TAGS).orEmpty()
                val untagged = entry.savedStateHandle.get<Boolean>(FilterResultKeys.UNTAGGED) ?: false
                val q = entry.savedStateHandle.get<String>(FilterResultKeys.NAME_CONTAINS).orEmpty()
                val sortRaw = entry.savedStateHandle.get<String>(FilterResultKeys.SORT)
                val dateFromRaw = entry.savedStateHandle.get<String>(FilterResultKeys.DATE_FROM)
                val dateToRaw = entry.savedStateHandle.get<String>(FilterResultKeys.DATE_TO)
                val importFromRaw = entry.savedStateHandle.get<String>(FilterResultKeys.IMPORT_FROM)
                val importToRaw = entry.savedStateHandle.get<String>(FilterResultKeys.IMPORT_TO)
                val sourceTab =
                    entry.savedStateHandle
                        .get<String>(FilterResultKeys.SOURCE_TAB)
                        ?.let { name -> MainTab.entries.find { it.name == name } }
                AppLog.d(
                    "Nav",
                    "Main filterApplied sourceTab=$sourceTab selectedNow=" +
                        "${mainViewModel.uiState.value.selectedTab} " +
                        "tagsRawLen=${tagsRaw.length} untagged=$untagged qLen=${q.length}",
                )
                mainViewModel.onEvent(
                    MainUiEvent.SetTagFilter(
                        criteria =
                            TagFilterCriteria(
                                selectedTagNames = TagFilterCriteria.parseTagsParam(tagsRaw),
                                includeUntagged = untagged,
                                nameContains = q,
                                sort =
                                    com.pictureorganizer.model.ImageListSort
                                        .fromParam(sortRaw),
                                dateTakenFromEpochDay = TagFilterCriteria.parseEpochDayParam(dateFromRaw),
                                dateTakenToEpochDay = TagFilterCriteria.parseEpochDayParam(dateToRaw),
                                importedAtFromEpochDay = TagFilterCriteria.parseEpochDayParam(importFromRaw),
                                importedAtToEpochDay = TagFilterCriteria.parseEpochDayParam(importToRaw),
                            ),
                        targetTab = sourceTab,
                    ),
                )
                entry.savedStateHandle[FilterResultKeys.APPLIED] = false
            }

            MainScreen(
                viewModel = mainViewModel,
                onNavigateToImport = {
                    navController.navigate(Routes.IMPORT_IMAGES)
                },
                onNavigateToDetail = { imageId ->

                    entry.savedStateHandle[DETAIL_VISIBLE_SIBLING_COUNT_KEY] =
                        mainViewModel.uiState.value.totalCount
                    navController.navigate(Routes.imageDetail(imageId))
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                },
                onNavigateToFilter = { criteria ->

                    val source = mainViewModel.uiState.value.selectedTab
                    entry.savedStateHandle[FilterResultKeys.SOURCE_TAB] = source.name
                    AppLog.d("Nav", "navigate filter SOURCE_TAB=$source active=${criteria.isActive}")
                    navController.navigate(Routes.filter(criteria))
                },
                onNavigateToExportZip = {
                    navController.navigate(Routes.EXPORT_ZIP)
                },
            )
        }
        composable(Routes.EXPORT_ZIP) { entry ->
            val exportVm: ExportZipViewModel =
                viewModel(
                    factory = ExportZipViewModel.Factory(repository, app.fileManager),
                )
            val filterApplied by entry.savedStateHandle
                .getStateFlow(FilterResultKeys.APPLIED, false)
                .collectAsStateWithLifecycle()

            LaunchedEffect(filterApplied) {
                if (!filterApplied) return@LaunchedEffect
                val tagsRaw = entry.savedStateHandle.get<String>(FilterResultKeys.TAGS).orEmpty()
                val untagged = entry.savedStateHandle.get<Boolean>(FilterResultKeys.UNTAGGED) ?: false
                val q = entry.savedStateHandle.get<String>(FilterResultKeys.NAME_CONTAINS).orEmpty()
                val sortRaw = entry.savedStateHandle.get<String>(FilterResultKeys.SORT)
                val dateFromRaw = entry.savedStateHandle.get<String>(FilterResultKeys.DATE_FROM)
                val dateToRaw = entry.savedStateHandle.get<String>(FilterResultKeys.DATE_TO)
                val importFromRaw = entry.savedStateHandle.get<String>(FilterResultKeys.IMPORT_FROM)
                val importToRaw = entry.savedStateHandle.get<String>(FilterResultKeys.IMPORT_TO)
                exportVm.onEvent(
                    ExportZipUiEvent.SetFilter(
                        TagFilterCriteria(
                            selectedTagNames = TagFilterCriteria.parseTagsParam(tagsRaw),
                            includeUntagged = untagged,
                            nameContains = q,
                            sort =
                                com.pictureorganizer.model.ImageListSort
                                    .fromParam(sortRaw),
                            dateTakenFromEpochDay = TagFilterCriteria.parseEpochDayParam(dateFromRaw),
                            dateTakenToEpochDay = TagFilterCriteria.parseEpochDayParam(dateToRaw),
                            importedAtFromEpochDay = TagFilterCriteria.parseEpochDayParam(importFromRaw),
                            importedAtToEpochDay = TagFilterCriteria.parseEpochDayParam(importToRaw),
                        ),
                    ),
                )
                entry.savedStateHandle[FilterResultKeys.APPLIED] = false
            }

            ExportZipScreen(
                viewModel = exportVm,
                onBack =
                    dropUnlessResumed {
                        navController.popRouteIfOnTop(Routes.EXPORT_ZIP, "exportZipBack")
                    },
                onNavigateToFilter = { criteria ->
                    navController.navigate(Routes.filter(criteria))
                },
                onNavigateToExportManage = {
                    navController.navigate(Routes.EXPORT_MANAGE)
                },
            )
        }
        composable(Routes.EXPORT_MANAGE) {
            val manageVm: ExportManageViewModel =
                viewModel(factory = ExportManageViewModel.Factory(app.fileManager))
            ExportManageScreen(
                viewModel = manageVm,
                onBack =
                    dropUnlessResumed {
                        navController.popRouteIfOnTop(Routes.EXPORT_MANAGE, "exportManageBack")
                    },
            )
        }
        composable(Routes.IMPORT_IMAGES) {
            ImportScreen(
                onBack = { onFailed ->
                    val ok = navController.popRouteIfOnTop(Routes.IMPORT_IMAGES, "importBack")
                    if (!ok) {
                        onFailed()
                    }
                },
            )
        }
        composable(
            route = Routes.IMAGE_DETAIL,
            arguments = listOf(navArgument("imageId") { type = NavType.StringType }),
        ) { entry ->
            val imageId = entry.arguments?.getString("imageId").orEmpty()

            val visibleSiblingCount =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<Int>(DETAIL_VISIBLE_SIBLING_COUNT_KEY)
                    ?: 0
            ImageDetailScreen(
                imageId = imageId,
                onBack =
                    dropUnlessResumed {
                        navController.popRouteIfOnTop("image-detail", "detailBack")
                    },
                visibleSiblingCount = visibleSiblingCount,
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack =
                    dropUnlessResumed {
                        navController.popRouteIfOnTop(Routes.SETTINGS, "settingsBack")
                    },
                onNavigateToTagManage = {
                    navController.navigate(Routes.TAG_MANAGE)
                },
                onNavigateToRenameTemplates = {
                    navController.navigate(Routes.RENAME_TEMPLATE_MANAGE)
                },
                onNavigateToDefaultTags = {
                    navController.navigate(Routes.DEFAULT_TAGS)
                },
                onNavigateToTutorial = {
                    navController.navigate(Routes.tutorial(fromSettings = true))
                },
                onNavigateToOssLicenses = {
                    navController.navigate(Routes.OSS_LICENSES)
                },
                onNavigateToExportManage = {
                    navController.navigate(Routes.EXPORT_MANAGE)
                },
            )
        }
        composable(Routes.OSS_LICENSES) {
            OssLicensesScreen(
                onBack =
                    dropUnlessResumed {
                        navController.popRouteIfOnTop(Routes.OSS_LICENSES, "ossLicensesBack")
                    },
            )
        }
        composable(Routes.TAG_MANAGE) {
            TagManageScreen(
                onBack =
                    dropUnlessResumed {
                        navController.popRouteIfOnTop(Routes.TAG_MANAGE, "tagManageBack")
                    },
            )
        }
        composable(Routes.RENAME_TEMPLATE_MANAGE) { entry ->
            val vm: RenameTemplateManageViewModel =
                viewModel(
                    factory = RenameTemplateManageViewModel.Factory(renameTemplateRepository),
                )
            val savedFlag by entry.savedStateHandle
                .getStateFlow(RENAME_TEMPLATE_SAVED_KEY, false)
                .collectAsStateWithLifecycle()
            LaunchedEffect(savedFlag) {
                if (savedFlag) {
                    entry.savedStateHandle[RENAME_TEMPLATE_SAVED_KEY] = false
                    vm.onEvent(RenameTemplateManageUiEvent.NotifySaved)
                }
            }
            RenameTemplateManageScreen(
                viewModel = vm,
                onBack =
                    dropUnlessResumed {
                        navController.popRouteIfOnTop(
                            Routes.RENAME_TEMPLATE_MANAGE,
                            "renameTemplateManageBack",
                        )
                    },
                onAdd = { navController.navigate(Routes.renameTemplateEdit()) },
                onEdit = { id -> navController.navigate(Routes.renameTemplateEdit(id)) },
            )
        }
        composable(Routes.RENAME_TEMPLATE_EDIT) {
            val vm: RenameTemplateEditViewModel =
                viewModel(
                    factory =
                        RenameTemplateEditViewModel.Factory(
                            renameTemplateRepository,
                            templateId = null,
                        ),
                )
            RenameTemplateEditScreen(
                viewModel = vm,
                onBack =
                    dropUnlessResumed {
                        navController.popRouteIfOnTop(
                            Routes.RENAME_TEMPLATE_EDIT,
                            "renameTemplateEditBack",
                        )
                    },
                onSaved =
                    dropUnlessResumed {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(RENAME_TEMPLATE_SAVED_KEY, true)
                        navController.popRouteIfOnTop(
                            Routes.RENAME_TEMPLATE_EDIT,
                            "renameTemplateEditSaved",
                        )
                    },
            )
        }
        composable(
            route = Routes.RENAME_TEMPLATE_EDIT_ID,
            arguments = listOf(navArgument("templateId") { type = NavType.StringType }),
        ) { entry ->
            val templateId = entry.arguments?.getString("templateId")
            val vm: RenameTemplateEditViewModel =
                viewModel(
                    factory =
                        RenameTemplateEditViewModel.Factory(
                            renameTemplateRepository,
                            templateId = templateId,
                        ),
                )
            RenameTemplateEditScreen(
                viewModel = vm,
                onBack =
                    dropUnlessResumed {
                        navController.popRouteIfOnTop(
                            "rename-template-edit",
                            "renameTemplateEditIdBack",
                        )
                    },
                onSaved =
                    dropUnlessResumed {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(RENAME_TEMPLATE_SAVED_KEY, true)
                        navController.popRouteIfOnTop(
                            "rename-template-edit",
                            "renameTemplateEditIdSaved",
                        )
                    },
            )
        }
        composable(Routes.DEFAULT_TAGS) {
            DefaultTagsScreen(
                tagRepository = tagRepository,
                userPreferences = userPreferences,
                onBack =
                    dropUnlessResumed {
                        navController.popRouteIfOnTop(Routes.DEFAULT_TAGS, "defaultTagsBack")
                    },
            )
        }
        composable(
            route = Routes.FILTER,
            arguments =
                listOf(
                    navArgument("tags") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("untagged") {
                        type = NavType.BoolType
                        defaultValue = false
                    },
                    navArgument("q") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("sort") {
                        type = NavType.StringType
                        defaultValue = com.pictureorganizer.model.ImageListSort.ImportedAtDesc.name
                    },
                    navArgument("dateFrom") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("dateTo") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("importFrom") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("importTo") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
        ) { entry ->
            val tagsRaw = Uri.decode(entry.arguments?.getString("tags").orEmpty())
            val untagged = entry.arguments?.getBoolean("untagged") ?: false
            val q = Uri.decode(entry.arguments?.getString("q").orEmpty())
            val sort =
                com.pictureorganizer.model.ImageListSort.fromParam(
                    entry.arguments?.getString("sort"),
                )
            val dateFrom =
                TagFilterCriteria.parseEpochDayParam(entry.arguments?.getString("dateFrom"))
            val dateTo =
                TagFilterCriteria.parseEpochDayParam(entry.arguments?.getString("dateTo"))
            val importFrom =
                TagFilterCriteria.parseEpochDayParam(entry.arguments?.getString("importFrom"))
            val importTo =
                TagFilterCriteria.parseEpochDayParam(entry.arguments?.getString("importTo"))
            val initial =
                TagFilterCriteria(
                    selectedTagNames = TagFilterCriteria.parseTagsParam(tagsRaw),
                    includeUntagged = untagged,
                    nameContains = q,
                    sort = sort,
                    dateTakenFromEpochDay = dateFrom,
                    dateTakenToEpochDay = dateTo,
                    importedAtFromEpochDay = importFrom,
                    importedAtToEpochDay = importTo,
                )
            val filterViewModel: FilterViewModel =
                viewModel(
                    factory = FilterViewModel.Factory(tagRepository, initial),
                )
            FilterScreen(
                viewModel = filterViewModel,
                onBack =
                    dropUnlessResumed {
                        navController.popFilterIfOnTop("filterBack")
                    },
                onApplied = { criteria ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set(FilterResultKeys.TAGS, criteria.encodeTagsParam())
                        set(FilterResultKeys.UNTAGGED, criteria.includeUntagged)
                        set(FilterResultKeys.NAME_CONTAINS, criteria.nameContains)
                        set(FilterResultKeys.SORT, criteria.sort.name)
                        set(
                            FilterResultKeys.DATE_FROM,
                            criteria.encodeEpochDayParam(criteria.dateTakenFromEpochDay),
                        )
                        set(
                            FilterResultKeys.DATE_TO,
                            criteria.encodeEpochDayParam(criteria.dateTakenToEpochDay),
                        )
                        set(
                            FilterResultKeys.IMPORT_FROM,
                            criteria.encodeEpochDayParam(criteria.importedAtFromEpochDay),
                        )
                        set(
                            FilterResultKeys.IMPORT_TO,
                            criteria.encodeEpochDayParam(criteria.importedAtToEpochDay),
                        )
                        set(FilterResultKeys.APPLIED, true)
                    }
                    navController.popFilterIfOnTop("filterApply")
                },
            )
        }
    }
}
