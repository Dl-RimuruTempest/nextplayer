package dev.anilbeesetti.nextplayer.feature.player.ui.controls

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import dev.anilbeesetti.nextplayer.core.model.VideoContentScale
import dev.anilbeesetti.nextplayer.core.ui.R
import dev.anilbeesetti.nextplayer.core.ui.extensions.copy
import dev.anilbeesetti.nextplayer.feature.player.buttons.LoopButton
import dev.anilbeesetti.nextplayer.feature.player.buttons.PlayerButton
import dev.anilbeesetti.nextplayer.feature.player.extensions.drawableRes

@OptIn(UnstableApi::class)
@Composable
fun ControlsTopView(
    modifier: Modifier = Modifier,
    player: Player,
    title: String,
    videoContentScale: VideoContentScale,
    hasChapters: Boolean,
    onPlaybackSpeedClick: () -> Unit = {},
    onPlaylistClick: () -> Unit = {},
    onChaptersClick: () -> Unit = {},
    onPictureInPictureClick: () -> Unit = {},
    onVideoContentScaleClick: () -> Unit = {},
    onVideoContentScaleLongClick: () -> Unit = {},
    onScreenshotClick: () -> Unit = {},
    onPlayInBackgroundClick: () -> Unit = {},
    onRotateClick: () -> Unit = {},
    onBackClick: () -> Unit,
) {
    val systemBarsPadding = WindowInsets.systemBars.union(WindowInsets.displayCutout).asPaddingValues()
    Column(
        modifier = modifier
            .padding(systemBarsPadding.copy(bottom = 0.dp))
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp),
    ) {
        // Top row: back, title, right-side buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PlayerButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_left),
                    contentDescription = null,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Video Zoom
                PlayerButton(
                    onClick = onVideoContentScaleClick,
                    onLongClick = onVideoContentScaleLongClick,
                ) {
                    Icon(
                        painter = painterResource(videoContentScale.drawableRes()),
                        contentDescription = null,
                    )
                }
                // Chapters — only show if video has chapters
                if (hasChapters) {
                    PlayerButton(onClick = onChaptersClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chapters),
                            contentDescription = null,
                        )
                    }
                }
                // Picture-in-Picture
                PlayerButton(onClick = onPictureInPictureClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_pip),
                        contentDescription = null,
                    )
                }
                // Playlist
                PlayerButton(onClick = onPlaylistClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_playlist),
                        contentDescription = null,
                    )
                }
            }
        }

        // Second row: below back button on the left
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(start = 4.dp, top = 4.dp),
        ) {
            LoopButton(player = player)
            PlayerButton(onClick = onPlaybackSpeedClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_speed),
                    contentDescription = null,
                )
            }
            PlayerButton(onClick = onScreenshotClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_screenshot),
                    contentDescription = null,
                )
            }
            PlayerButton(onClick = onPlayInBackgroundClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_headset),
                    contentDescription = null,
                )
            }
            PlayerButton(onClick = onRotateClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_screen_rotation),
                    contentDescription = null,
                )
            }
        }
    }
}
