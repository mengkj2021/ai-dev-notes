package com.pictureorganizer.ui.defaulttags

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pictureorganizer.R
import com.pictureorganizer.data.repository.TagRepository
import com.pictureorganizer.data.repository.UserPreferencesRepository
import com.pictureorganizer.ui.common.BackNavIconButton
import com.pictureorganizer.ui.common.LogScreenLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTagsScreen(
    tagRepository: TagRepository,
    userPreferences: UserPreferencesRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LogScreenLifecycle("DefaultTags")
    val libraryTags by tagRepository.observeTags().collectAsStateWithLifecycle(emptyList())
    val stored by userPreferences.defaultTagNames.collectAsStateWithLifecycle(emptySet())
    var userEdited by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf(emptySet<String>()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(stored) {
        if (!userEdited) {
            draft = stored
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.default_tags_title)) },
                navigationIcon = {
                    BackNavIconButton(
                        onBack = onBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                userPreferences.setDefaultTagNames(draft)
                                onBack()
                            }
                        },
                    ) {
                        Text(stringResource(R.string.detail_tag_save))
                    }
                },
            )
        },
    ) { innerPadding ->
        if (libraryTags.isEmpty()) {
            Text(
                text = stringResource(R.string.default_tags_empty_library),
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .padding(24.dp),
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.default_tags_hint),
                        modifier = Modifier.padding(16.dp),
                    )
                }
                items(libraryTags, key = { it.id }) { tag ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    ) {
                        Checkbox(
                            checked = tag.name in draft,
                            onCheckedChange = { checked ->
                                userEdited = true
                                draft = if (checked) draft + tag.name else draft - tag.name
                            },
                        )
                        Text(tag.name)
                    }
                }
            }
        }
    }
}
