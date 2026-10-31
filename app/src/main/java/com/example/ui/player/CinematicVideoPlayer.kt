package com.example.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.avatar.MotionAvatarView
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CinematicVideoPlayer(
    project: CinematicProject,
    currentPlaybackSeconds: Float,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onSeekSeconds: (Float) -> Unit,
    onOpenVoiceModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = project.totalDurationSeconds.coerceAtLeast(1.0f)
    val progress = (currentPlaybackSeconds / totalSeconds).coerceIn(0f, 1f)

    // Identify which shot is currently playing
    var accumulated = 0f
    var activeShotIndex = 0
    for (i in project.shots.indices) {
        val shot = project.shots[i]
        if (currentPlaybackSeconds <= accumulated + shot.durationSeconds || i == project.shots.lastIndex) {
            activeShotIndex = i
            break
        }
        accumulated += shot.durationSeconds
    }
    val currentShot = project.shots.getOrNull(activeShotIndex) ?: project.shots.first()

    // Normalized progress within the current shot (0f..1f)
    val shotStart = project.shots.take(activeShotIndex).sumOf { it.durationSeconds.toDouble() }.toFloat()
    val shotProgress = ((currentPlaybackSeconds - shotStart) / currentShot.durationSeconds.coerceAtLeast(0.1f)).coerceIn(0f, 1f)

    // Continuous loop animation for particles and optical lens flare
    val infiniteTransition = rememberInfiniteTransition(label = "cinematic_fx")
    val fxPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing)
        ),
        label = "fx_phase"
    )

    // Calculate camera motion offsets
    val panOffsetX = when (currentShot.cameraMotion) {
        CameraMotion.PAN_LEFT -> (1f - shotProgress) * 40f - 20f
        CameraMotion.PAN_RIGHT -> shotProgress * 40f - 20f
        CameraMotion.ORBITAL_DRONE -> sin(shotProgress * Math.PI.toFloat()) * 25f
        else -> 0f
    }
    val zoomScale = when (currentShot.cameraMotion) {
        CameraMotion.DOLLY_IN -> 1.0f + (shotProgress * 0.15f)
        CameraMotion.DOLLY_OUT -> 1.15f - (shotProgress * 0.15f)
        CameraMotion.FPV_DIVE -> 1.0f + (shotProgress * 0.28f)
        else -> 1.05f
    }
    val blurAmount = if (currentShot.cameraMotion == CameraMotion.RACK_FOCUS) {
        (sin(shotProgress * Math.PI.toFloat()) * 8f).dp
    } else 0.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cinematic_video_player"),
        colors = CardDefaults.cardColors(containerColor = CineVoid),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(CineBorder, CineVoid)))
    ) {
        Column {
            // Viewport Screen with Letterboxing Aspect Ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clipToBounds()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Background Cinematic Artwork / Frame
                val imageRes = currentShot.imageDrawableRes ?: when (activeShotIndex % 3) {
                    0 -> R.drawable.img_hero_cyberpunk
                    1 -> R.drawable.img_hero_fantasy
                    else -> R.drawable.img_hero_cyberpunk
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = panOffsetX.dp)
                        .scale(zoomScale)
                        .blur(blurAmount)
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = currentShot.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Color Grading (LUT Tint)
                val tintColor = Color(project.colorGrade.tintColor)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tintColor.copy(alpha = 0.16f))
                )

                // Atmospheric Visual Filters Overlay (Canvas rendering)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    when (currentShot.visualFilter) {
                        VisualFilter.ANAMORPHIC_FLARE -> {
                            // Horizontal anamorphic cyan beam
                            val flareY = h * 0.45f + sin(fxPhase * 6.28f) * 15f
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        CineCyan.copy(alpha = 0.85f),
                                        CineGold.copy(alpha = 0.95f),
                                        CineCyan.copy(alpha = 0.85f),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(0f, flareY),
                                end = Offset(w, flareY),
                                strokeWidth = 3.dp.toPx()
                            )
                            // Central lens flare iris circle
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(CineGoldLight.copy(alpha = 0.6f), Color.Transparent),
                                    center = Offset(w * 0.5f, flareY),
                                    radius = 60.dp.toPx()
                                )
                            )
                        }

                        VisualFilter.VOLUMETRIC_FOG -> {
                            // Drifting fog gradients
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.05f),
                                        Color.White.copy(alpha = 0.22f),
                                        Color.Transparent
                                    ),
                                    startY = h * fxPhase,
                                    endY = h * (fxPhase + 0.6f)
                                )
                            )
                        }

                        VisualFilter.EMBER_PARTICLES -> {
                            // Floating fire sparks
                            repeat(18) { i ->
                                val px = (w * ((i * 0.055f + fxPhase * 0.4f) % 1f))
                                val py = h * (1f - ((i * 0.06f + fxPhase) % 1f))
                                drawCircle(
                                    color = if (i % 2 == 0) CineGold else CineNeonRed,
                                    radius = (1.5f + (i % 3)).dp.toPx(),
                                    center = Offset(px, py)
                                )
                            }
                        }

                        VisualFilter.RAIN_MIST -> {
                            // Falling rain streaks
                            repeat(24) { i ->
                                val rx = (w * ((i * 0.042f + fxPhase * 0.7f) % 1f))
                                val ry = (h * ((i * 0.08f + fxPhase * 2f) % 1f))
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(rx, ry),
                                    end = Offset(rx - 8f, ry + 24f),
                                    strokeWidth = 1.2.dp.toPx()
                                )
                            }
                        }

                        VisualFilter.FILM_GRAIN -> {
                            // Subtle film grain specks
                            repeat(40) { i ->
                                val gx = (w * ((i * 0.026f + fxPhase * 1.5f) % 1f))
                                val gy = (h * ((i * 0.038f + fxPhase * 0.9f) % 1f))
                                drawRect(
                                    color = Color.White.copy(alpha = 0.15f),
                                    topLeft = Offset(gx, gy),
                                    size = Size(2.dp.toPx(), 2.dp.toPx())
                                )
                            }
                        }

                        else -> Unit
                    }
                }

                // CinemaScope 2.39:1 Letterbox Matte Bars (Top & Bottom)
                if (project.aspectRatio == AspectRatio.CINEMASCOPE) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .align(Alignment.TopCenter)
                            .background(Color.Black)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .align(Alignment.BottomCenter)
                            .background(Color.Black)
                    )
                }

                // Composited 100% Free Motion Avatar (Picture in Picture or corner)
                if (project.avatarConfig.isEnabled) {
                    val avatar = AvatarProfile.FREE_AVATARS.find { it.id == project.avatarConfig.selectedAvatarId }
                        ?: AvatarProfile.FREE_AVATARS.first()

                    val alignment = when (project.avatarConfig.placement) {
                        AvatarPlacement.BOTTOM_LEFT -> Alignment.BottomStart
                        AvatarPlacement.BOTTOM_RIGHT -> Alignment.BottomEnd
                        AvatarPlacement.PICTURE_IN_PICTURE -> Alignment.TopEnd
                        AvatarPlacement.CENTER_STAGE -> Alignment.Center
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 22.dp),
                        contentAlignment = alignment
                    ) {
                        MotionAvatarView(
                            avatar = avatar,
                            isSpeaking = isPlaying,
                            sizeDp = (80.dp * (project.avatarConfig.scaleFraction / 0.35f)),
                            showFreeBadge = true,
                            showSpeakingWave = true
                        )
                    }
                }

                // Shot Title Badge & Camera Motion Indicator (Top Left)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .background(Color(0xCC090A0F), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isPlaying) CineNeonRed else CineGold, CircleShape)
                    )
                    Text(
                        text = "TOMA ${activeShotIndex + 1}/${project.shots.size} • ${currentShot.cameraMotion.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CineTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 4K UHD & Aspect Ratio Badge (Top Right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .background(Color(0xCC090A0F), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = project.resolution.label.take(6),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CineCyan
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = CineTextMuted
                    )
                    Text(
                        text = project.aspectRatio.badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CineGold
                    )
                }

                // Cinematic Dynamic Subtitles / Avatar Narration (Bottom Overlay)
                if (currentShot.narrationText.isNotBlank() || project.avatarConfig.scriptText.isNotBlank()) {
                    val displayText = if (isPlaying && project.avatarConfig.isEnabled && project.avatarConfig.scriptText.isNotBlank()) {
                        project.avatarConfig.scriptText
                    } else if (currentShot.narrationText.isNotBlank()) {
                        currentShot.narrationText
                    } else {
                        currentShot.promptDescription
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = if (project.aspectRatio == AspectRatio.CINEMASCOPE) 22.dp else 12.dp)
                            .background(Color(0xAA08090D), RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CineGoldLight,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2
                        )
                    }
                }
            }

            // Player Timeline Bar & Scrubber Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Slider Scrubber
                Slider(
                    value = progress,
                    onValueChange = { newProg ->
                        onSeekSeconds(newProg * totalSeconds)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("timeline_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = CineGold,
                        activeTrackColor = CineGold,
                        inactiveTrackColor = CineSurfaceElevated
                    )
                )

                // Controls Row: Play/Pause, Timecode, Voice Mic Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Play/Pause and Replay
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledIconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("play_pause_button"),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = CineGold,
                                contentColor = CineVoid
                            )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { onSeekSeconds(0f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Reiniciar",
                                tint = CineTextSecondary
                            )
                        }

                        // Timecode Display
                        val currentMin = (currentPlaybackSeconds.toInt() / 60)
                        val currentSec = (currentPlaybackSeconds.toInt() % 60)
                        val currentMs = ((currentPlaybackSeconds % 1f) * 100).toInt()
                        val totalMin = (totalSeconds.toInt() / 60)
                        val totalSec = (totalSeconds.toInt() % 60)

                        Text(
                            text = String.format("%02d:%02d.%02d / %02d:%02d", currentMin, currentSec, currentMs, totalMin, totalSec),
                            style = MaterialTheme.typography.labelMedium,
                            color = CineTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Right: Voice Command Trigger Mic Button
                    Button(
                        onClick = onOpenVoiceModal,
                        modifier = Modifier
                            .testTag("voice_command_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CineCyan.copy(alpha = 0.2f),
                            contentColor = CineCyan
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(CineCyan, CineGold))),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Editar con Voz",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Editar con Voz",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
