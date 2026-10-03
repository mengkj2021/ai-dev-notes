package com.pictureorganizer.ui.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

internal sealed class TutorialBodyBlock {
    data class Paragraph(
        val text: String,
    ) : TutorialBodyBlock()

    data class NumberedItem(
        val number: String,
        val title: String,
        val body: String,
    ) : TutorialBodyBlock()
}

private val numberedLine = Regex("""^(\d+)\.\s+(.+)$""")

internal fun parseTutorialBody(raw: String): List<TutorialBodyBlock> {
    val lines = raw.replace("\r\n", "\n").replace('\r', '\n').lines()
    val result = mutableListOf<TutorialBodyBlock>()
    val paragraphLines = mutableListOf<String>()
    var currentNumber: String? = null
    var currentTitle: String? = null
    val currentBody = mutableListOf<String>()

    fun flushParagraph() {
        val text = paragraphLines.joinToString("\n").trim()
        if (text.isNotEmpty()) {
            result.add(TutorialBodyBlock.Paragraph(text))
        }
        paragraphLines.clear()
    }

    fun flushItem() {
        val number = currentNumber ?: return
        val title = currentTitle ?: return
        result.add(
            TutorialBodyBlock.NumberedItem(
                number = number,
                title = title,
                body = currentBody.joinToString("\n").trim(),
            ),
        )
        currentNumber = null
        currentTitle = null
        currentBody.clear()
    }

    for (line in lines) {
        val trimmed = line.trim()
        val match = numberedLine.matchEntire(trimmed)
        when {
            match != null -> {
                flushParagraph()
                flushItem()
                currentNumber = match.groupValues[1]
                currentTitle = match.groupValues[2]
            }
            trimmed.isEmpty() -> {
                if (currentNumber != null) {
                    flushItem()
                } else {
                    flushParagraph()
                }
            }
            else -> {
                if (currentNumber != null) {
                    currentBody.add(trimmed)
                } else {
                    paragraphLines.add(trimmed)
                }
            }
        }
    }
    flushParagraph()
    flushItem()
    return result
}

@Composable
internal fun TutorialBodyText(
    raw: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(raw) { parseTutorialBody(raw) }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        for (block in blocks) {
            when (block) {
                is TutorialBodyBlock.Paragraph -> {
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                is TutorialBodyBlock.NumberedItem -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "${block.number}. ${block.title}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Start,
                        )
                        if (block.body.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = block.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Start,
                            )
                        }
                    }
                }
            }
        }
    }
}
