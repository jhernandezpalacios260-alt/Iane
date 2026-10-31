package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.player.CinematicVideoPlayer
import com.example.ui.theme.*
import com.example.ui.timeline.MultiTrackTimelineView

@Composable
fun EditorStudioScreen(
    project: CinematicProject,
    currentPlaybackSeconds: Float,
    isPlaying: Boolean,
    selectedShotIndex: Int,
    lastVoiceCommandResult: VoiceCommandResult?,
    onTogglePlay: () -> Unit,
    onSeekSeconds: (Float) -> Unit,
    onSelectShot: (Int) -> Unit,
    onUpdateProject: (CinematicProject) -> Unit,
    onOpenVoiceModal: () -> Unit,
    onNavigateToAvatars: () -> Unit,
    onNavigateToPostproduction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeShot = project.shots.getOrNull(selectedShotIndex) ?: project.shots.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CineVoid)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Voice Command Feedback Banner (if recently executed)
        if (lastVoiceCommandResult != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CineSurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CineCyan, CineGold)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Comando", tint = CineCyan, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Comando de Voz: \"${lastVoiceCommandResult.spokenText}\"",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CineCyan
                            )
                            Text(
                                text = lastVoiceCommandResult.changesApplied.joinToString(" • "),
                                fontSize = 11.sp,
                                color = CineTextPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Live Cinema Video Player Viewport
        item {
            CinematicVideoPlayer(
                project = project,
                currentPlaybackSeconds = currentPlaybackSeconds,
                isPlaying = isPlaying,
                onTogglePlay = onTogglePlay,
                onSeekSeconds = onSeekSeconds,
                onOpenVoiceModal = onOpenVoiceModal
            )
        }

        // Multi-Track Timeline Sequencer
        item {
            MultiTrackTimelineView(
                project = project,
                selectedShotIndex = selectedShotIndex,
                onSelectShot = onSelectShot,
                onAddShot = {
                    val newShot = CinematicShot(
                        id = "shot_${System.currentTimeMillis()}",
                        title = "Nueva Toma Aérea",
                        promptDescription = "Plano aéreo cinemático de revelación con iluminación dramática",
                        durationSeconds = 3.5f,
                        cameraMotion = CameraMotion.DOLLY_IN,
                        visualFilter = VisualFilter.ANAMORPHIC_FLARE
                    )
                    onUpdateProject(project.copy(shots = project.shots + newShot))
                },
                onEditShotDuration = { index, duration ->
                    val updated = project.shots.mapIndexed { idx, s ->
                        if (idx == index) s.copy(durationSeconds = duration) else s
                    }
                    onUpdateProject(project.copy(shots = updated))
                },
                onDeleteShot = { index ->
                    if (project.shots.size > 1) {
                        val updated = project.shots.filterIndexed { idx, _ -> idx != index }
                        onUpdateProject(project.copy(shots = updated))
                    }
                },
                onNavigateToAvatars = onNavigateToAvatars,
                onNavigateToPostproduction = onNavigateToPostproduction
            )
        }

        // Selected Shot Fine-Tuning Controls
        if (activeShot != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CineSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "AJUSTES DE TOMA ${selectedShotIndex + 1}: ${activeShot.title}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CineGold
                            )
                            // Duration stepper
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        if (activeShot.durationSeconds > 1.0f) {
                                            val updated = project.shots.mapIndexed { idx, s ->
                                                if (idx == selectedShotIndex) s.copy(durationSeconds = s.durationSeconds - 0.5f) else s
                                            }
                                            onUpdateProject(project.copy(shots = updated))
                                        }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Menos", tint = CineTextSecondary)
                                }

                                Text(
                                    text = "${activeShot.durationSeconds}s",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = CineTextPrimary
                                )

                                IconButton(
                                    onClick = {
                                        val updated = project.shots.mapIndexed { idx, s ->
                                            if (idx == selectedShotIndex) s.copy(durationSeconds = s.durationSeconds + 0.5f) else s
                                        }
                                        onUpdateProject(project.copy(shots = updated))
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = "Más", tint = CineGold)
                                }
                            }
                        }

                        // Camera Motion Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Movimiento de Cámara", style = MaterialTheme.typography.labelSmall, color = CineTextSecondary)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(CameraMotion.entries) { motion ->
                                    val isSel = motion == activeShot.cameraMotion
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) CineCyan else CineSurfaceVariant,
                                        modifier = Modifier.clickable {
                                            val updated = project.shots.mapIndexed { idx, s ->
                                                if (idx == selectedShotIndex) s.copy(cameraMotion = motion) else s
                                            }
                                            onUpdateProject(project.copy(shots = updated))
                                        }
                                    ) {
                                        Text(
                                            text = motion.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) CineVoid else CineTextPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Optical Visual Filter Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Efecto Óptico & Atmósfera", style = MaterialTheme.typography.labelSmall, color = CineTextSecondary)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(VisualFilter.entries) { filter ->
                                    val isSel = filter == activeShot.visualFilter
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) CineGold else CineSurfaceVariant,
                                        modifier = Modifier.clickable {
                                            val updated = project.shots.mapIndexed { idx, s ->
                                                if (idx == selectedShotIndex) s.copy(visualFilter = filter) else s
                                            }
                                            onUpdateProject(project.copy(shots = updated))
                                        }
                                    ) {
                                        Text(
                                            text = filter.label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) CineVoid else CineTextPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Speed Ramp
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Rampa de Velocidad", style = MaterialTheme.typography.labelSmall, color = CineTextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SpeedRamp.entries.forEach { ramp ->
                                    val isSel = ramp == activeShot.speedRamp
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSel) CineEmerald else CineSurfaceVariant,
                                        modifier = Modifier.clickable {
                                            val updated = project.shots.mapIndexed { idx, s ->
                                                if (idx == selectedShotIndex) s.copy(speedRamp = ramp) else s
                                            }
                                            onUpdateProject(project.copy(shots = updated))
                                        }
                                    ) {
                                        Text(
                                            text = ramp.label,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) CineVoid else CineTextPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
