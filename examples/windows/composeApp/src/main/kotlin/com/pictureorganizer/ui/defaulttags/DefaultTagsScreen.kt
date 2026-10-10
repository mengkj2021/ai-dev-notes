package com.pictureorganizer.ui.defaulttags

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.data.repository.UserPreferencesRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTagsScreen(
    tags: TagRepository,
    prefs: UserPreferencesRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tagList by tags.observeTags().collectAsState(initial = emptyList())
    val prefState by prefs.prefs.collectAsState()
    var draft by remember(prefState.defaultTagNames) { mutableStateOf(prefState.defaultTagNames.toSet()) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("默认标签") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                prefs.setDefaultTagNames(draft)
                                onBack()
                            }
                        },
                    ) { Text("保存") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            Text("导入时自动合并到新图的标签（需先在标签库添加）。")
            if (tagList.isEmpty()) {
                Text("标签库为空，请先去标签管理添加。", modifier = Modifier.padding(top = 16.dp))
            } else {
                LazyColumn {
                    items(tagList, key = { it.id }) { tag ->
                        Row {
                            Checkbox(
                                checked = tag.name in draft,
                                onCheckedChange = { checked ->
                                    draft = if (checked) draft + tag.name else draft - tag.name
                                },
                            )
                            Text(tag.name, modifier = Modifier.padding(top = 12.dp))
                        }
                    }
                }
            }
        }
    }
}
