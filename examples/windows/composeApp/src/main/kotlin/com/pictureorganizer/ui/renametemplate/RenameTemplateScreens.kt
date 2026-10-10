package com.pictureorganizer.ui.renametemplate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pictureorganizer.data.repository.RenameTemplateRepository
import kotlinx.coroutines.launch

val RenamePatternInsertTokens =
    listOf("{name}", "{date}", "{tag}", "{yyyy}", "{mm}", "{dd}", "{time}", "{tags}", "{n}")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameTemplateManageScreen(
    repo: RenameTemplateRepository,
    onBack: () -> Unit,
    onEdit: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val list by repo.observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("重命名模板") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { onEdit(null) }) { Text("添加") }
                },
            )
        },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            items(list, key = { it.id }) { item ->
                ListItem(
                    headlineContent = {
                        Text(buildString {
                            append(item.name)
                            if (item.isDefault) append(" ★")
                        })
                    },
                    supportingContent = { Text(item.pattern) },
                    trailingContent = {
                        androidx.compose.foundation.layout.Row {
                            TextButton(onClick = { scope.launch { repo.setDefault(item.id) } }) { Text("默认") }
                            TextButton(onClick = { onEdit(item.id) }) { Text("编辑") }
                            TextButton(onClick = { scope.launch { repo.delete(item.id) } }) { Text("删") }
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RenameTemplateEditScreen(
    repo: RenameTemplateRepository,
    templateId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf("{name}_{date}") }
    var isDefault by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(templateId) {
        if (templateId != null) {
            val existing = repo.getById(templateId)
            if (existing != null) {
                name = existing.name
                pattern = existing.pattern
                isDefault = existing.isDefault
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (templateId == null) "添加重命名模板" else "编辑重命名模板") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val result =
                                    if (templateId == null) {
                                        repo.insert(name, pattern, isDefault)
                                    } else {
                                        repo.update(templateId, name, pattern, isDefault)
                                    }
                                if (result.isSuccess) onBack() else message = result.exceptionOrNull()?.message
                            }
                        },
                    ) { Text("保存") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = pattern,
                onValueChange = { pattern = it },
                label = { Text("规则") },
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RenamePatternInsertTokens.forEach { token ->
                    AssistChip(
                        onClick = { pattern += token },
                        label = { Text(token) },
                    )
                }
            }
            androidx.compose.foundation.layout.Row {
                Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                Text("设为默认", modifier = Modifier.padding(top = 12.dp))
            }
            message?.let { Text(it) }
        }
    }
}
