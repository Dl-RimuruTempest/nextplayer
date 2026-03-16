package dev.anilbeesetti.nextplayer.feature.player.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.common.listen

data class Chapter(
    val title: String,
    val startPositionMs: Long,
)

@Composable
fun rememberMetadataState(player: Player): MetadataState {
    val metadataState = remember { MetadataState(player) }
    LaunchedEffect(player) { metadataState.observe() }
    return metadataState
}

@Stable
class MetadataState(private val player: Player) {
    var title: String? by mutableStateOf(null)
        private set

    var chapters: List<Chapter> by mutableStateOf(emptyList())
        private set

    suspend fun observe() {
        title = player.mediaMetadata.title?.toString()
        chapters = extractChapters()
        player.listen { events ->
            if (events.containsAny(
                    Player.EVENT_MEDIA_METADATA_CHANGED,
                    Player.EVENT_MEDIA_ITEM_TRANSITION,
                )
            ) {
                title = player.mediaMetadata.title?.toString()
                chapters = extractChapters()
            }
        }
    }

    private fun extractChapters(): List<Chapter> {
    val mediaChapters = player.mediaMetadata.chapters
    if (mediaChapters.isNullOrEmpty()) return emptyList()
    
    return mediaChapters.mapNotNull { chapter ->
        val chapterTitle = chapter.title?.toString()?.takeIf { it.isNotBlank() }
            ?: "Chapter ${chapter.startTimeMs}"
        Chapter(
            title = chapterTitle,
            startPositionMs = chapter.startTimeMs
        )
    }
}
