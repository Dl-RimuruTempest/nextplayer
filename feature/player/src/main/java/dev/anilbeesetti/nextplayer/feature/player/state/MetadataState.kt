package dev.anilbeesetti.nextplayer.feature.player.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.listen
import androidx.media3.common.util.UnstableApi

data class Chapter(
    val title: String,
    val startPositionMs: Long,
)

@UnstableApi
@Composable
fun rememberMetadataState(player: Player): MetadataState {
    val metadataState = remember { MetadataState(player) }
    LaunchedEffect(player) { metadataState.observe() }
    return metadataState
}

@UnstableApi
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
                    Player.EVENT_TIMELINE_CHANGED,
                )
            ) {
                title = player.mediaMetadata.title?.toString()
                chapters = extractChapters()
            }
        }
    }

    private fun extractChapters(): List<Chapter> {
        return try {
            val timeline = player.currentTimeline
            if (timeline.isEmpty) return emptyList()
            val windowIndex = player.currentMediaItemIndex
            val window = Timeline.Window()
            timeline.getWindow(windowIndex, window)
            val windowDurationUs = window.durationUs
            if (windowDurationUs == C.TIME_UNSET || windowDurationUs <= 0) return emptyList()

            val periodCount = timeline.periodCount
            if (periodCount < 2) return emptyList()

            val result = mutableListOf<Chapter>()
            val period = Timeline.Period()
            for (i in 0 until periodCount) {
                timeline.getPeriod(i, period, true)
                // Only include periods that belong to the current window
                if (period.windowIndex != windowIndex) continue
                val startUs = period.positionInWindowUs
                if (startUs < 0) continue
                val startMs = startUs / 1000
                // Use period id as title if it looks like a chapter name
                val id = period.id?.toString() ?: ""
                val title = when {
                    id.isNotBlank() && !id.matches(Regex("\\d+")) -> id
                    else -> "Chapter ${result.size + 1}"
                }
                result.add(Chapter(title = title, startPositionMs = startMs))
            }
            if (result.size < 2) emptyList() else result
        } catch (_: Exception) {
            emptyList()
        }
    }
}
