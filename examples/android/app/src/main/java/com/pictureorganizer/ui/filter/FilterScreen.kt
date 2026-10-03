package com.pictureorganizer.ui.filter

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pictureorganizer.R
import com.pictureorganizer.model.ImageListSort
import com.pictureorganizer.model.TagFilterCriteria
import com.pictureorganizer.ui.common.BackNavIconButton
import com.pictureorganizer.ui.common.LogScreenLifecycle
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: FilterViewModel,
    onBack: () -> Unit,
    onApplied: (TagFilterCriteria) -> Unit,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("Filter")
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pickingField by remember { mutableStateOf<DateField?>(null) }

    var closing by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    var lifecycleState by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState)
    }
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, _ ->
                lifecycleState = lifecycleOwner.lifecycle.currentState
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    fun requestBack() {
        if (closing || !isResumed) return
        closing = true
        onBack()
    }

    fun requestApply(criteria: TagFilterCriteria) {
        if (closing) return
        closing = true
        onApplied(criteria)
    }

    BackHandler {
        requestBack()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is FilterUiEffect.ApplyAndClose -> requestApply(effect.criteria)
            }
        }
    }

    pickingField?.let { field ->
        val initialEpochDay =
            when (field) {
                DateField.TakenFrom -> state.dateTakenFromEpochDay
                DateField.TakenTo -> state.dateTakenToEpochDay
                DateField.ImportFrom -> state.importedAtFromEpochDay
                DateField.ImportTo -> state.importedAtToEpochDay
            }
        DateTakenPickerDialog(
            initialEpochDay = initialEpochDay,
            onDismiss = { pickingField = null },
            onConfirm = { epochDay ->
                when (field) {
                    DateField.TakenFrom ->
                        viewModel.onEvent(FilterUiEvent.DateTakenFromChanged(epochDay))
                    DateField.TakenTo ->
                        viewModel.onEvent(FilterUiEvent.DateTakenToChanged(epochDay))
                    DateField.ImportFrom ->
                        viewModel.onEvent(FilterUiEvent.ImportedAtFromChanged(epochDay))
                    DateField.ImportTo ->
                        viewModel.onEvent(FilterUiEvent.ImportedAtToChanged(epochDay))
                }
                pickingField = null
            },
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.filter_title)) },
                navigationIcon = {
                    BackNavIconButton(
                        onBack = { requestBack() },
                        contentDescription = stringResource(R.string.action_back),
                    )
                },
            )
        },
        bottomBar = {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { viewModel.onEvent(FilterUiEvent.ClearAndApply) }) {
                    Text(stringResource(R.string.action_filter_clear))
                }
                TextButton(onClick = { viewModel.onEvent(FilterUiEvent.Apply) }) {
                    Text(stringResource(R.string.filter_apply))
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
        ) {
            OutlinedTextField(
                value = state.nameContains,
                onValueChange = { viewModel.onEvent(FilterUiEvent.NameContainsChanged(it)) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                singleLine = true,
                label = { Text(stringResource(R.string.filter_name_contains)) },
            )
            Text(
                text = stringResource(R.string.filter_sort_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
            SortOption(
                label = stringResource(R.string.filter_sort_imported_desc),
                selected = state.sort == ImageListSort.ImportedAtDesc,
                onClick = {
                    viewModel.onEvent(FilterUiEvent.SortChanged(ImageListSort.ImportedAtDesc))
                },
            )
            SortOption(
                label = stringResource(R.string.filter_sort_imported_asc),
                selected = state.sort == ImageListSort.ImportedAtAsc,
                onClick = {
                    viewModel.onEvent(FilterUiEvent.SortChanged(ImageListSort.ImportedAtAsc))
                },
            )
            SortOption(
                label = stringResource(R.string.filter_sort_name_asc),
                selected = state.sort == ImageListSort.NameAsc,
                onClick = {
                    viewModel.onEvent(FilterUiEvent.SortChanged(ImageListSort.NameAsc))
                },
            )
            SortOption(
                label = stringResource(R.string.filter_sort_date_taken_desc),
                selected = state.sort == ImageListSort.DateTakenDesc,
                onClick = {
                    viewModel.onEvent(FilterUiEvent.SortChanged(ImageListSort.DateTakenDesc))
                },
            )
            SortOption(
                label = stringResource(R.string.filter_sort_date_taken_asc),
                selected = state.sort == ImageListSort.DateTakenAsc,
                onClick = {
                    viewModel.onEvent(FilterUiEvent.SortChanged(ImageListSort.DateTakenAsc))
                },
            )
            Text(
                text = stringResource(R.string.filter_date_taken_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
            DateBoundRow(
                label = stringResource(R.string.filter_date_taken_from),
                epochDay = state.dateTakenFromEpochDay,
                onPick = { pickingField = DateField.TakenFrom },
                onClear = { viewModel.onEvent(FilterUiEvent.DateTakenFromChanged(null)) },
            )
            DateBoundRow(
                label = stringResource(R.string.filter_date_taken_to),
                epochDay = state.dateTakenToEpochDay,
                onPick = { pickingField = DateField.TakenTo },
                onClear = { viewModel.onEvent(FilterUiEvent.DateTakenToChanged(null)) },
            )
            Text(
                text = stringResource(R.string.filter_imported_at_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
            DateBoundRow(
                label = stringResource(R.string.filter_imported_at_from),
                epochDay = state.importedAtFromEpochDay,
                onPick = { pickingField = DateField.ImportFrom },
                onClear = { viewModel.onEvent(FilterUiEvent.ImportedAtFromChanged(null)) },
            )
            DateBoundRow(
                label = stringResource(R.string.filter_imported_at_to),
                epochDay = state.importedAtToEpochDay,
                onPick = { pickingField = DateField.ImportTo },
                onClear = { viewModel.onEvent(FilterUiEvent.ImportedAtToChanged(null)) },
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.includeUntagged,
                    onCheckedChange = { viewModel.onEvent(FilterUiEvent.ToggleUntagged) },
                )
                Text(stringResource(R.string.filter_untagged))
            }
            state.availableTags.forEach { name ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = name in state.selectedTagNames,
                        onCheckedChange = { viewModel.onEvent(FilterUiEvent.ToggleTag(name)) },
                    )
                    Text(name)
                }
            }
            if (state.availableTags.isEmpty()) {
                Text(
                    text = stringResource(R.string.filter_no_library_tags),
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
    }
}

private enum class DateField {
    TakenFrom,
    TakenTo,
    ImportFrom,
    ImportTo,
}

@Composable
private fun DateBoundRow(
    label: String,
    epochDay: Long?,
    onPick: () -> Unit,
    onClear: () -> Unit,
) {
    val valueText =
        epochDay?.let { LocalDate.ofEpochDay(it).format(DATE_DISPLAY) }
            ?: stringResource(R.string.filter_date_taken_unset)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(text = valueText, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = onPick) {
            Text(stringResource(R.string.filter_date_taken_pick))
        }
        if (epochDay != null) {
            TextButton(onClick = onClear) {
                Text(stringResource(R.string.filter_date_taken_clear))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTakenPickerDialog(
    initialEpochDay: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
) {
    val initialMillis =
        initialEpochDay?.let { epochDayToUtcMillis(it) }
            ?: remember { System.currentTimeMillis() }
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis ?: return@TextButton
                    onConfirm(utcMillisToEpochDay(millis))
                },
            ) {
                Text(stringResource(R.string.filter_date_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.filter_date_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun SortOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}

private val DATE_DISPLAY: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private fun epochDayToUtcMillis(epochDay: Long): Long =
    LocalDate
        .ofEpochDay(epochDay)
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

private fun utcMillisToEpochDay(millis: Long): Long =
    Instant
        .ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .toEpochDay()
