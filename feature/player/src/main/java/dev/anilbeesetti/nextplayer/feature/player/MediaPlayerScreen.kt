package dev.anilbeesetti.nextplayer.feature.player

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import dev.anilbeesetti.nextplayer.core.model.PlayerPreferences
import dev.anilbeesetti.nextplayer.core.ui.R as coreUiR
import dev.anilbeesetti.nextplayer.core.ui.extensions.copy
import dev.anilbeesetti.nextplayer.feature.player.buttons.PlayerButton
import dev.anilbeesetti.nextplayer.feature.player.state.ControlsVisibilityState
import dev.anilbeesetti.nextplayer.feature.player.state.VerticalGesture
import dev.anilbeesetti.nextplayer.feature.player.state.rememberBrightnessState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberControlsVisibilityState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberErrorState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberMediaPresentationState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberMetadataState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberPictureInPictureState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberRotationState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberSeekGestureState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberTapGestureState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberVideoZoomAndContentScaleState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberVolumeAndBrightnessGestureState
import dev.anilbeesetti.nextplayer.feature.player.state.rememberVolumeState
import dev.anilbeesetti.nextplayer.feature.player.extensions.nameRes
import dev.anilbeesetti.nextplayer.feature.player.state.seekAmountFormatted
import dev.anilbeesetti.nextplayer.feature.player.state.seekToPositionFormated
import dev.anilbeesetti.nextplayer.feature.player.ui.DoubleTapIndicator
import dev.anilbeesetti.nextplayer.feature.player.ui.OverlayShowView
import dev.anilbeesetti.nextplayer.feature.player.ui.OverlayView
import dev.anilbeesetti.nextplayer.feature.player.ui.SubtitleConfiguration
import dev.anilbeesetti.nextplayer.feature.player.ui.VerticalProgressView
import dev.anilbeesetti.nextplayer.feature.player.ui.controls.ControlsBottomView
import dev.anilbeesetti.nextplayer.feature.player.ui.controls.ControlsTopView
import dev.anilbeesetti.nextplayer.feature.player.utils.captureScreenshot
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.launch

val LocalControlsVisibilityState = compositionLocalOf<ControlsVisibilityState?> { null }

@OptIn(UnstableApi::class)
@Composable
fun MediaPlayerScreen(
    player: Player?,
    viewModel: PlayerViewModel,
    playerPreferences: PlayerPreferences,
    modifier: Modifier = Modifier,
    onSelectSubtitleClick: () -> Unit,
    onBackClick: () -> Unit,
    onPlayInBackgroundClick: () -> Unit,
) {
    val volumeState = rememberVolumeState(
        player = player,
        showVolumePanelIfHeadsetIsOn = playerPreferences.showSystemVolumePanel,
    )
    player ?: return
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val metadataState = rememberMetadataState(player)
    val mediaPresentationState = rememberMediaPresentationState(player)
    val controlsVisibilityState = rememberControlsVisibilityState(
        player = player,
        hideAfter = playerPreferences.controllerAutoHideTimeout.seconds,
    )
    val tapGestureState = rememberTapGestureState(
        player = player,
        doubleTapGesture = playerPreferences.doubleTapGesture,
        seekIncrementMillis = playerPreferences.seekIncrement.seconds.inWholeMilliseconds,
        useLongPressGesture = playerPreferences.useLongPressControls,
        longPressSpeed = playerPreferences.longPressControlsSpeed,
    )
    val seekGestureState = rememberSeekGestureState(
        player = player,
        sensitivity = playerPreferences.seekSensitivity,
        enableSeekGesture = playerPreferences.useSeekControls,
    )
    val pictureInPictureState = rememberPictureInPictureState(
        player = player,
        autoEnter = playerPreferences.autoPip,
    )
    val videoZoomAndContentScaleState = rememberVideoZoomAndContentScaleState(
        player = player,
        initialContentScale = playerPreferences.playerVideoZoom,
        enableZoomGesture = playerPreferences.useZoomControls,
        enablePanGesture = playerPreferences.enablePanGesture,
        onEvent = viewModel::onVideoZoomEvent,
    )
    val brightnessState = rememberBrightnessState()
    val volumeAndBrightnessGestureState = rememberVolumeAndBrightnessGestureState(
        volumeState = volumeState,
        brightnessState = brightnessState,
        enableVolumeGesture = playerPreferences.enableVolumeSwipeGesture,
        enableBrightnessGesture = playerPreferences.enableBrightnessSwipeGesture,
        volumeGestureSensitivity = playerPreferences.volumeGestureSensitivity,
        brightnessGestureSensitivity = playerPreferences.brightnessGestureSensitivity,
    )
    val rotationState = rememberRotationState(
        player = player,
        screenOrientation = playerPreferences.playerScreenOrientation,
    )
    val errorState = rememberErrorState(player = player)
    val seekIncrementMs = playerPreferences.seekIncrement.seconds.inWholeMilliseconds

    LaunchedEffect(pictureInPictureState.isInPictureInPictureMode) {
        if (pictureInPictureState.isInPictureInPictureMode) {
            controlsVisibilityState.hideControls()
        }
    }

    LaunchedEffect(tapGestureState.isLongPressGestureInAction) {
        if (tapGestureState.isLongPressGestureInAction) {
            controlsVisibilityState.hideControls()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        if (playerPreferences.rememberPlayerBrightness) {
            brightnessState.setBrightness(playerPreferences.playerBrightness)
        }
    }

    LaunchedEffect(brightnessState.currentBrightness) {
        if (playerPreferences.rememberPlayerBrightness) {
            viewModel.updatePlayerBrightness(brightnessState.currentBrightness)
        }
    }

    var overlayView by remember { mutableStateOf<OverlayView?>(null) }

    CompositionLocalProvider(LocalControlsVisibilityState provides controlsVisibilityState) {
        Box {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black),
            ) {
                PlayerContentFrame(
                    player = player,
                    pictureInPictureState = pictureInPictureState,
                    controlsVisibilityState = controlsVisibilityState,
                    tapGestureState = tapGestureState,
                    seekGestureState = seekGestureState,
                    videoZoomAndContentScaleState = videoZoomAndContentScaleState,
                    volumeAndBrightnessGestureState = volumeAndBrightnessGestureState,
                    subtitleConfiguration = SubtitleConfiguration(
                        useSystemCaptionStyle = playerPreferences.useSystemCaptionStyle,
                        showBackground = playerPreferences.subtitleBackground,
                        font = playerPreferences.subtitleFont,
                        textSize = playerPreferences.subtitleTextSize,
                        textBold = playerPreferences.subtitleTextBold,
                        applyEmbeddedStyles = playerPreferences.applyEmbeddedStyles,
                    ),
                )

                AnimatedVisibility(
                    visible = controlsVisibilityState.controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Box(
                        modifier = modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                    )
                }

                // Show buffering spinner only when buffering AND not seeking
                if (mediaPresentationState.isBuffering && !seekGestureState.isSeeking) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(72.dp),
                    )
                }

                DoubleTapIndicator(tapGestureState = tapGestureState)

                AnimatedVisibility(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .align(Alignment.TopCenter),
                    visible = tapGestureState.isLongPressGestureInAction,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Surface(shape = CircleShape) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 8.dp,
                            ),
                        ) {
                            Text(
                                text = stringResource(coreUiR.string.fast_playback_speed, tapGestureState.longPressSpeed),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }

                if (controlsVisibilityState.controlsVisible && controlsVisibilityState.controlsLocked) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding()
                            .padding(top = 24.dp),
                    ) {
                        PlayerButton(onClick = { controlsVisibilityState.unlockControls() }) {
                            Icon(
                                painter = painterResource(coreUiR.drawable.ic_lock),
                                contentDescription = stringResource(coreUiR.string.controls_unlock),
                            )
                        }
                    }
                } else {
                    PlayerControlsView(
                        topView = {
                            AnimatedVisibility(
                                visible = controlsVisibilityState.controlsVisible,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                ControlsTopView(
                                    player = player,
                                    title = metadataState.title ?: "",
                                    videoContentScale = videoZoomAndContentScaleState.videoContentScale,
                                    hasChapters = mediaPresentationState.chapters.isNotEmpty(),
                                    onPlaybackSpeedClick = {
                                        controlsVisibilityState.hideControls()
                                        overlayView = OverlayView.PLAYBACK_SPEED
                                    },
                                    onPlaylistClick = {
                                        controlsVisibilityState.hideControls()
                                        overlayView = OverlayView.PLAYLIST
                                    },
                                    onChaptersClick = {
                                        controlsVisibilityState.hideControls()
                                        overlayView = OverlayView.CHAPTER_SELECTOR
                                    },
                                    onPictureInPictureClick = {
                                        if (!pictureInPictureState.hasPipPermission) {
                                            Toast.makeText(context, coreUiR.string.enable_pip_from_settings, Toast.LENGTH_SHORT).show()
                                            pictureInPictureState.openPictureInPictureSettings()
                                        } else {
                                            pictureInPictureState.enterPictureInPictureMode()
                                        }
                                    },
                                    onVideoContentScaleClick = {
                                        controlsVisibilityState.showControls()
                                        videoZoomAndContentScaleState.switchToNextVideoContentScale()
                                    },
                                    onVideoContentScaleLongClick = {
                                        controlsVisibilityState.hideControls()
                                        overlayView = OverlayView.VIDEO_CONTENT_SCALE
                                    },
                                    onScreenshotClick = {
                                        coroutineScope.launch {
                                            captureScreenshot(context, player)
                                        }
                                    },
                                    onPlayInBackgroundClick = onPlayInBackgroundClick,
                                    onRotateClick = rotationState::rotate,
                                    onBackClick = onBackClick,
                                )
                            }
                        },
                        middleView = {
                            when {
                                seekGestureState.seekAmount != null -> InfoView(info = "${seekGestureState.seekAmountFormatted}\n[${seekGestureState.seekToPositionFormated}]")
                                videoZoomAndContentScaleState.isZooming -> InfoView(info = "${(videoZoomAndContentScaleState.zoom * 100).toInt()}%")
                                videoZoomAndContentScaleState.showContentScaleIndicator -> InfoView(info = stringResource(videoZoomAndContentScaleState.videoContentScale.nameRes()))
                                else -> Unit
                            }
                        },
                        bottomView = {
                            AnimatedVisibility(
                                visible = controlsVisibilityState.controlsVisible && !controlsVisibilityState.controlsLocked,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                ControlsBottomView(
                                    player = player,
                                    mediaPresentationState = mediaPresentationState,
                                    seekIncrementMs = seekIncrementMs,
                                    onSeek = seekGestureState::onSeek,
                                    onSeekEnd = seekGestureState::onSeekEnd,
                                    onLockControlsClick = {
                                        controlsVisibilityState.showControls()
                                        controlsVisibilityState.lockControls()
                                    },
                                    onAudioClick = {
                                        controlsVisibilityState.hideControls()
                                        overlayView = OverlayView.AUDIO_SELECTOR
                                    },
                                    onSubtitleClick = {
                                        controlsVisibilityState.hideControls()
                                        overlayView = OverlayView.SUBTITLE_SELECTOR
                                    },
                                )
                            }
                        },
                    )
                }

                val systemBarsPadding = WindowInsets.systemBars.asPaddingValues()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .displayCutoutPadding()
                        .padding(systemBarsPadding.copy(top = 0.dp, bottom = 0.dp))
                        .padding(24.dp),
                ) {
                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.CenterStart),
                        visible = volumeAndBrightnessGestureState.activeGesture == VerticalGesture.VOLUME,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        VerticalProgressView(
                            value = volumeState.volumePercentage,
                            maxValue = volumeState.maxVolumePercentage,
                            icon = painterResource(coreUiR.drawable.ic_volume),
                        )
                    }

                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        visible = volumeAndBrightnessGestureState.activeGesture == VerticalGesture.BRIGHTNESS,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        VerticalProgressView(
                            value = brightnessState.brightnessPercentage,
                            icon = painterResource(coreUiR.drawable.ic_brightness),
                        )
                    }
                }
            }

            OverlayShowView(
                player = player,
                overlayView = overlayView,
                videoContentScale = videoZoomAndContentScaleState.videoContentScale,
                chapters = mediaPresentationState.chapters,
                currentChapter = mediaPresentationState.currentChapter,
                onChapterSelected = { chapter ->
                    player.seekTo(chapter.startPositionMs)
                },
                onDismiss = { overlayView = null },
                onSelectSubtitleClick = onSelectSubtitleClick,
                onSubtitleOptionEvent = viewModel::onSubtitleOptionEvent,
                onVideoContentScaleChanged = { videoZoomAndContentScaleState.onVideoContentScaleChanged(it) },
            )
        }
    }

    errorState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(text = stringResource(coreUiR.string.error_playing_video))
            },
            text = {
                Text(text = error.message ?: stringResource(coreUiR.string.unknown_error))
            },
            confirmButton = {
                if (player.hasNextMediaItem()) {
                    TextButton(
                        onClick = {
                            errorState.dismiss()
                            player.seekToNext()
                            player.play()
                        },
                    ) {
                        Text(text = stringResource(coreUiR.string.play_next_video))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        errorState.dismiss()
                        onBackClick()
                    },
                ) {
                    Text(text = stringResource(coreUiR.string.exit))
                }
            },
        )
    }

    BackHandler {
        if (overlayView != null) {
            overlayView = null
        } else {
            onBackClick()
        }
    }
}

@Composable
fun InfoView(
    modifier: Modifier = Modifier,
    info: String,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = info,
            style = textStyle,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun PlayerControlsView(
    modifier: Modifier = Modifier,
    topView: @Composable () -> Unit,
    middleView: @Composable BoxScope.() -> Unit,
    bottomView: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            topView()
            Spacer(modifier = Modifier.weight(1f))
            bottomView()
        }
        middleView()
    }
}
