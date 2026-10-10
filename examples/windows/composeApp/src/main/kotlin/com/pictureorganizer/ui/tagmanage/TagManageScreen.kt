package com.pictureorganizer.ui.tagmanage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pictureorganizer.data.repository.ImageRepository
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.model.Tag
import com.pictureorganizer.model.TagTemplate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagManageScreen(
    tags: TagRepository,
    images: ImageRepository,
    prefs: UserPreferencesRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableStateOf(0) }
    val tagList by tags.observeTags().collectAsState(initial = emptyList())
    val templates by tags.observeTemplates().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    var editingTag by remember { mutableStateOf<Tag?>(null) }
    var draftName by remember { mutableStateOf("") }
    var confirmDeleteTag by remember { mutableStateOf<Tag?>(null) }
    var editingTemplate by remember { mutableStateOf<TagTemplate?>(null) }
    var creatingTemplate by remember { mutableStateOf(false) }
    var templateName by remember { mutableStateOf("") }
    var templateSelected by remember { mutableStateOf(setOf<String>()) }
    var templateDefault by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("标签管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (tab == 0) {
                                editingTag = null
                                draftName = ""
                            } else {
                                creatingTemplate = true
                                editingTemplate = null
                                templateName = ""
                                templateSelected = emptySet()
                                templateDefault = false
                            }
                        },
                    ) { Text(if (tab == 0) "清空输入" else "添加模板") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("标签") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("模板") })
            }
            message?.let { Text(it, modifier = Modifier.padding(12.dp)) }
            if (tab == 0) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = draftName,
                        onValueChange = { draftName = it },
                        label = { Text(if (editingTag == null) "新标签名" else "改名：${editingTag!!.name}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    TextButton(
                        onClick = {
                            scope.launch {
                                val result =
                                    if (editingTag == null) {
                                        tags.insertTag(draftName)
                                    } else {
                                        val old = editingTag!!.name
                                        val newName = draftName.trim()
                                        if (newName != old && images.countImagesWithTag(old) > 0) {
                                            images.renameTagInAllImages(old, newName)
                                            prefs.renameDefaultTagName(old, newName)
                                        }
                                        tags.updateTag(editingTag!!.id, draftName)
                                    }
                                message = result.exceptionOrNull()?.message ?: "已保存"
                                if (result.isSuccess) {
                                    editingTag = null
                                    draftName = ""
                                }
                            }
                        },
                    ) { Text("保存") }
                }
                LazyColumn {
                    items(tagList, key = { it.id }) { tag ->
                        ListItem(
                            headlineContent = { Text(tag.name) },
                            trailingContent = {
                                Row {
                                    TextButton(
                                        onClick = {
                                            editingTag = tag
                                            draftName = tag.name
                                        },
                                    ) { Text("改名") }
                                    TextButton(onClick = { confirmDeleteTag = tag }) { Text("删除") }
                                }
                            },
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(templates, key = { it.id }) { template ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    buildString {
                                        append(template.name)
                                        if (template.isDefault) append(" ★")
                                    },
                                )
                            },
                            supportingContent = { Text(template.tagNames.joinToString()) },
                            trailingContent = {
                                Row {
                                    TextButton(
                                        onClick = { scope.launch { tags.setDefaultTemplate(template.id) } },
                                    ) { Text("默认") }
                                    TextButton(
                                        onClick = {
                                            creatingTemplate = false
                                            editingTemplate = template
                                            templateName = template.name
                                            templateSelected = template.tagNames.toSet()
                                            templateDefault = template.isDefault
                                        },
                                    ) { Text("编辑") }
                                    TextButton(
                                        onClick = { scope.launch { tags.deleteTemplate(template.id) } },
                                    ) { Text("删") }
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    confirmDeleteTag?.let { tag ->
        AlertDialog(
            onDismissRequest = { confirmDeleteTag = null },
            title = { Text("删除标签？") },
            text = { Text("将从所有图片与默认标签中移除「${tag.name}」。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            images.removeTagFromAllImages(tag.name)
                            prefs.removeDefaultTagName(tag.name)
                            tags.deleteTag(tag.id)
                            confirmDeleteTag = null
                            message = "已删除"
                        }
                    },
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteTag = null }) { Text("取消") }
            },
        )
    }

    if (creatingTemplate || editingTemplate != null) {
        AlertDialog(
            onDismissRequest = {
                creatingTemplate = false
                editingTemplate = null
            },
            title = { Text(if (editingTemplate == null) "添加标签模板" else "编辑标签模板") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = templateName,
                        onValueChange = { templateName = it },
                        label = { Text("模板名") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("勾选标签")
                    tagList.forEach { tag ->
                        Row {
                            Checkbox(
                                checked = tag.name in templateSelected,
                                onCheckedChange = { checked ->
                                    templateSelected =
                                        if (checked) templateSelected + tag.name else templateSelected - tag.name
                                },
                            )
                            Text(tag.name, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                    Row {
                        Checkbox(checked = templateDefault, onCheckedChange = { templateDefault = it })
                        Text("设为默认模板", modifier = Modifier.padding(top = 12.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result =
                                if (editingTemplate == null) {
                                    tags.insertTemplate(templateName, templateSelected.toList(), templateDefault)
                                } else {
                                    tags.updateTemplate(
                                        editingTemplate!!.id,
                                        templateName,
                                        templateSelected.toList(),
                                        templateDefault,
                                    )
                                }
                            message = result.exceptionOrNull()?.message ?: "模板已保存"
                            if (result.isSuccess) {
                                creatingTemplate = false
                                editingTemplate = null
                            }
                        }
                    },
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        creatingTemplate = false
                        editingTemplate = null
                    },
                ) { Text("取消") }
            },
        )
    }
}
