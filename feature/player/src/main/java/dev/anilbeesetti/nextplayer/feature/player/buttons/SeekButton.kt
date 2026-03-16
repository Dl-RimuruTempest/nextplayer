package dev.anilbeesetti.nextplayer.feature.player.buttons

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun SeekForwardButton(
    player: Player,
    seekIncrementMs: Long,
    modifier: Modifier = Modifier,
) {
    val seconds = seekIncrementMs / 1000
    PlayerButton(
        modifier = modifier.size(48.dp),
        onClick = {
            player.seekTo((player.currentPosition + seekIncrementMs).coerceAtMost(player.duration))
        },
    ) {
        Text(
            text = "+${seconds}s",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun SeekBackButton(
    player: Player,
    seekIncrementMs: Long,
    modifier: Modifier = Modifier,
) {
    val seconds = seekIncrementMs / 1000
    PlayerButton(
        modifier = modifier.size(48.dp),
        onClick = {
            player.seekTo((player.currentPosition - seekIncrementMs).coerceAtLeast(0L))
        },
    ) {
        Text(
            text = "-${seconds}s",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
