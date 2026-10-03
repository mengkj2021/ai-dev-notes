package com.pictureorganizer.ui.main.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.pictureorganizer.PictureOrganizerApplication
import com.pictureorganizer.model.ImageListItem
import com.pictureorganizer.model.ImageStatus
import com.pictureorganizer.ui.theme.PictureOrganizerTheme
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImageListItemRow(
    item: ImageListItem,
    isEditMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (isEditMode) {
                        Modifier.clickable(onClick = onToggleSelect)
                    } else {
                        Modifier.clickable(onClick = onClick)
                    },
                ).padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isEditMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() },
            )
        }

        Thumbnail(
            item = item,
            modifier =
                Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp)),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ImageListItem.userTagsOf(item.tags).forEach { tag ->
                    AssistChip(
                        onClick = {},
                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
                        enabled = false,
                        colors =
                            AssistChipDefaults.assistChipColors(
                                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun Thumbnail(
    item: ImageListItem,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val placeholder = Color(item.placeholderColorArgb)
    val file = rememberImageFile(item.filePath)

    SubcomposeAsyncImage(
        model =
            ImageRequest
                .Builder(context)
                .data(file)
                .crossfade(true)
                .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(placeholder),
        loading = {
            PlaceholderThumb(placeholder)
        },
        error = {
            PlaceholderThumb(placeholder)
        },
    )
}

@Composable
private fun PlaceholderThumb(color: Color) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
private fun rememberImageFile(relativePath: String): File? {
    val context = LocalContext.current
    if (relativePath.isBlank()) return null
    val app = context.applicationContext as? PictureOrganizerApplication
    return if (app != null) {
        app.fileManager.absoluteFile(relativePath)
    } else {
        null
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageListItemRowPreview() {
    PictureOrganizerTheme {
        ImageListItemRow(
            item =
                ImageListItem(
                    id = "preview-01",
                    date = "2026-08-27",
                    description = "周末出游照片，待重命名",
                    tags = listOf("旅行"),
                    placeholderColorArgb = 0xFFE57373,
                    status = ImageStatus.Pending,
                    filePath = "pending/preview-01.jpg",
                ),
            isEditMode = true,
            isSelected = true,
            onToggleSelect = {},
        )
    }
}
