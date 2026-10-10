package com.pictureorganizer.ui.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pictureorganizer.model.ImageSort
import com.pictureorganizer.model.Tag
import com.pictureorganizer.model.TagFilterCriteria
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    initial: TagFilterCriteria,
    onBack: () -> Unit,
    onApply: (TagFilterCriteria) -> Unit,
    tagFlow: Flow<List<Tag>> = flowOf(emptyList()),
    availableTags: List<Tag> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val tagsFromFlow by tagFlow.collectAsState(initial = availableTags)
    val tagList = tagsFromFlow.ifEmpty { availableTags }
    var nameContains by remember { mutableStateOf(initial.nameContains) }
    var sort by remember { mutableStateOf(initial.sort) }
    var includeUntagged by remember { mutableStateOf(initial.includeUntagged) }
    var selectedTags by remember { mutableStateOf(initial.selectedTags) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("筛选") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            nameContains = ""
                            sort = ImageSort.ImportedAtDesc
                            includeUntagged = false
                            selectedTags = emptySet()
                        },
                    ) { Text("重置") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = nameContains,
                onValueChange = { nameContains = it },
                label = { Text("文件名包含") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Text("排序")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = sort == ImageSort.ImportedAtDesc,
                    onClick = { sort = ImageSort.ImportedAtDesc },
                    label = { Text("导入日") },
                )
                FilterChip(
                    selected = sort == ImageSort.NameAsc,
                    onClick = { sort = ImageSort.NameAsc },
                    label = { Text("文件名") },
                )
                FilterChip(
                    selected = sort == ImageSort.DateTakenDesc,
                    onClick = { sort = ImageSort.DateTakenDesc },
                    label = { Text("拍摄日") },
                )
            }
            FilterChip(
                selected = includeUntagged,
                onClick = { includeUntagged = !includeUntagged },
                label = { Text("包含未打标签") },
            )
            Text("标签（多选，OR）")
            if (tagList.isEmpty()) {
                Text("标签库为空")
            } else {
                tagList.forEach { tag ->
                    Row {
                        Checkbox(
                            checked = tag.name in selectedTags,
                            onCheckedChange = { checked ->
                                selectedTags =
                                    if (checked) selectedTags + tag.name else selectedTags - tag.name
                            },
                        )
                        Text(tag.name, modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
            Button(
                onClick = {
                    onApply(
                        TagFilterCriteria(
                            nameContains = nameContains,
                            sort = sort,
                            includeUntagged = includeUntagged,
                            selectedTags = selectedTags,
                        ),
                    )
                },
            ) { Text("应用") }
        }
    }
}
