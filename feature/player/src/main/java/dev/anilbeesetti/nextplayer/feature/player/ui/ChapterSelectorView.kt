package dev.anilbeesetti.nextplayer.feature.player.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.BoxScope
import dev.anilbeesetti.nextplayer.core.ui.components.NextSegmentedListItem
import dev.anilbeesetti.nextplayer.feature.player.state.Chapter
import dev.anilbeesetti.nextplayer.feature.player.extensions.formatted
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun BoxScope.ChapterSelectorView(
    show: Boolean,
    chapters: List<Chapter>,
    currentChapter: Chapter?,
    onChapterSelected: (Chapter) -> Unit,
    onDismiss: () -> Unit,
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(show) {
        if (show && chapters.isNotEmpty()) {
            val currentIndex = chapters.indexOf(currentChapter).takeIf { it >= 0 } ?: 0
            lazyListState.scrollToItem(currentIndex)
        }
    }

    OverlayView(
        show = show,
        title = "Chapters",
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(8.dp),
        ) {
            itemsIndexed(chapters) { index, chapter ->
                val isCurrentChapter = chapter == currentChapter
                NextSegmentedListItem(
                    modifier = Modifier.fillMaxWidth(),
                    selected = isCurrentChapter,
                    isFirstItem = index == 0,
                    isLastItem = index == chapters.lastIndex,
                    colors = ListItemDefaults.segmentedColors(
                        contentColor = if (isCurrentChapter) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            ListItemDefaults.segmentedColors().contentColor
                        },
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    ),
                    onClick = {
                        onChapterSelected(chapter)
                        onDismiss()
                    },
                    content = {
                        Text(
                            text = chapter.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    },
                    trailingContent = {
                        Text(
                            text = chapter.startPositionMs.milliseconds.formatted(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }
        }
    }
}
