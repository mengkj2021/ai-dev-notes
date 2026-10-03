package com.pictureorganizer.ui.main.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pictureorganizer.R
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.model.RenameTemplate
import com.pictureorganizer.ui.main.MainTab
import com.pictureorganizer.ui.main.statusNameRes

private val TabActionBarRowHeight = 48.dp
private val TabActionBarButtonPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
private val TabActionBarDropdownIconSize = 18.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabActionBar(
    currentTab: MainTab,
    isEditMode: Boolean,
    hasSelection: Boolean,
    isFilterActive: Boolean,
    filterSummaryLabels: List<String>,
    renameTemplates: List<RenameTemplate>,
    isBatchRenaming: Boolean,
    canEmptyTrash: Boolean,
    onEditClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onMoveTo: (ImageStatus) -> Unit,
    onApplyRenameTemplate: (String) -> Unit,
    onImportClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEmptyTrashClick: () -> Unit,
    onExportClick: () -> Unit,
    onFilterClick: () -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var moveMenuExpanded by remember { mutableStateOf(false) }
    var renameMenuExpanded by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(TabActionBarRowHeight)
                    .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onEditClick,
                    contentPadding = TabActionBarButtonPadding,
                ) {
                    Text(
                        text =
                            if (isEditMode) {
                                stringResource(R.string.action_done)
                            } else {
                                stringResource(R.string.action_edit)
                            },
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (isEditMode) {
                    Box {
                        TextButton(
                            onClick = { moveMenuExpanded = true },
                            enabled = hasSelection && !isBatchRenaming,
                            contentPadding = TabActionBarButtonPadding,
                        ) {
                            Text(
                                text = stringResource(R.string.action_move_to),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(TabActionBarDropdownIconSize),
                            )
                        }
                        DropdownMenu(
                            expanded = moveMenuExpanded,
                            onDismissRequest = { moveMenuExpanded = false },
                        ) {
                            moveTargets(currentTab).forEach { target ->
                                DropdownMenuItem(
                                    text = { Text(moveTargetLabel(target)) },
                                    onClick = {
                                        moveMenuExpanded = false
                                        onMoveTo(target)
                                    },
                                )
                            }
                        }
                    }
                    Box {
                        TextButton(
                            onClick = { renameMenuExpanded = true },
                            enabled =
                                hasSelection &&
                                    renameTemplates.isNotEmpty() &&
                                    !isBatchRenaming,
                            contentPadding = TabActionBarButtonPadding,
                        ) {
                            Text(
                                text = stringResource(R.string.action_rename),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(TabActionBarDropdownIconSize),
                            )
                        }
                        DropdownMenu(
                            expanded = renameMenuExpanded,
                            onDismissRequest = { renameMenuExpanded = false },
                        ) {
                            renameTemplates.forEach { template ->
                                DropdownMenuItem(
                                    text = { Text(template.name) },
                                    onClick = {
                                        renameMenuExpanded = false
                                        onApplyRenameTemplate(template.id)
                                    },
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.action_filter),
                    )
                }

                Box {
                    IconButton(onClick = { moreMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.action_more),
                        )
                    }
                    DropdownMenu(
                        expanded = moreMenuExpanded,
                        onDismissRequest = { moreMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_select_all)) },
                            enabled = isEditMode && !isBatchRenaming,
                            onClick = {
                                moreMenuExpanded = false
                                onSelectAllClick()
                            },
                        )
                        if (currentTab == MainTab.NoModify) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_delete)) },
                                enabled = isEditMode && hasSelection && !isBatchRenaming,
                                onClick = {
                                    moreMenuExpanded = false
                                    onDeleteClick()
                                },
                            )
                        }
                    }
                }
            }

            when (currentTab) {
                MainTab.Pending -> {
                    TextButton(
                        onClick = onImportClick,
                        contentPadding = TabActionBarButtonPadding,
                    ) {
                        Text(
                            text = stringResource(R.string.action_import),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                MainTab.NoModify -> {
                    TextButton(
                        onClick = onEmptyTrashClick,
                        enabled = canEmptyTrash && !isBatchRenaming,
                        contentPadding = TabActionBarButtonPadding,
                    ) {
                        Text(
                            text = stringResource(R.string.action_empty_trash),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                MainTab.Confirmed -> {
                    TextButton(
                        onClick = onExportClick,
                        contentPadding = TabActionBarButtonPadding,
                    ) {
                        Text(
                            text = stringResource(R.string.action_export_zip),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        if (isFilterActive && filterSummaryLabels.isNotEmpty()) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    filterSummaryLabels.forEach { label ->
                        AssistChip(
                            onClick = onFilterClick,
                            label = { Text(label) },
                        )
                    }
                }
                TextButton(onClick = onClearFilter) {
                    Text(stringResource(R.string.action_filter_clear))
                }
            }
        }
    }
}

private fun moveTargets(currentTab: MainTab): List<ImageStatus> =
    when (currentTab) {
        MainTab.Pending -> listOf(ImageStatus.Confirmed, ImageStatus.NoModify)
        MainTab.Confirmed -> listOf(ImageStatus.Pending, ImageStatus.NoModify)
        MainTab.NoModify -> listOf(ImageStatus.Pending, ImageStatus.Confirmed)
    }

@Composable
private fun moveTargetLabel(status: ImageStatus): String = stringResource(status.statusNameRes())
