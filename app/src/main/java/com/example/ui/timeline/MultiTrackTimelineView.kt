package com.example.ui.timeline

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun MultiTrackTimelineView(
    project: CinematicProject,
    selectedShotIndex: Int,
    onSelectShot: (Int) -> Unit,
    onAddShot: () -> Unit,
    onEditShotDuration: (Int, Float) -> Unit,
    onDeleteShot: (Int) -> Unit,
    onNavigateToAvatars: () -> Unit,
    onNavigateToPostproduction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("multi_track_timeline"),
        colors = CardDefaults.cardColors(containerColor = CineSurface),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(CineBorder, CineVoid)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Sequence title and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.ViewTimeline,
                        contentDescription = "Timeline",
                        tint = CineGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "LÍNEA DE TIEMPO MULTIPISTA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CineTextPrimary
                    )
                }

                Text(
                    text = "${project.shots.size} Tomas • Total: ${String.format("%.1f", project.totalDurationSeconds)}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = CineGold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Track 1: Video Shots Track
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "V1",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CineCyan,
                    modifier = Modifier
                        .background(CineCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Text(
                    text = "Pista de Tomas de Video",
                    style = MaterialTheme.typography.labelSmall,
                    color = CineTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Scrollable Shot Blocks
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                project.shots.forEachIndexed { index, shot ->
                    val isSelected = index == selectedShotIndex
                    val imageRes = shot.imageDrawableRes ?: when (index % 3) {
                        0 -> R.drawable.img_hero_cyberpunk
                        1 -> R.drawable.img_hero_fantasy
                        else -> R.drawable.img_hero_cyberpunk
                    }

                    Box(
                        modifier = Modifier
                            .width(135.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) CineSurfaceElevated else CineSurfaceVariant)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) CineGold else CineBorder,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectShot(index) }
                            .padding(6.dp)
                            .testTag("shot_card_$index")
                    ) {
                        Column {
                            // Thumbnail Preview with overlay pills
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = imageRes),
                                    contentDescription = shot.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                // Duration pill
                                Text(
                                    text = "${shot.durationSeconds}s",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Toma ${index + 1}: ${shot.title}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) CineGold else CineTextPrimary,
                                maxLines = 1
                            )

                            // Badges for Camera Motion and Filter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = shot.cameraMotion.name.take(6),
                                    fontSize = 8.sp,
                                    color = CineCyan,
                                    modifier = Modifier
                                        .background(CineCyan.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                                Text(
                                    text = shot.speedRamp.label.take(5),
                                    fontSize = 8.sp,
                                    color = CineGold,
                                    modifier = Modifier
                                        .background(CineGold.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Add Shot Button
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(85.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CineSurfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, CineBorder, RoundedCornerShape(10.dp))
                        .clickable { onAddShot() }
                        .testTag("add_shot_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Añadir Toma",
                            tint = CineGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+ Toma",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CineGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = CineBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Track 2: Avatars in Motion Track (100% Free)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CineSurfaceVariant)
                    .clickable { onNavigateToAvatars() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "AV",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CineNeonRed,
                        modifier = Modifier
                            .background(CineNeonRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Avatar con Movimiento & Voz",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = CineTextPrimary
                            )
                            Text(
                                text = "100% GRATIS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = CineGold,
                                modifier = Modifier
                                    .background(CineGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = if (project.avatarConfig.isEnabled) "Activo: Sophia • Lip-Sync & Gesto Dinámico" else "Desactivado",
                            style = MaterialTheme.typography.labelSmall,
                            color = CineTextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Editar Avatar",
                    tint = CineGold,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Track 3: Audio & Post-Production Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CineSurfaceVariant)
                    .clickable { onNavigateToPostproduction() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "FX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CineEmerald,
                        modifier = Modifier
                            .background(CineEmerald.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                    Column {
                        Text(
                            text = "LUT: ${project.colorGrade.label} • Audio: ${project.audioConfig.soundtrack.label.take(18)}...",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = CineTextPrimary
                        )
                        Text(
                            text = "Postproducción automatizada & Render ${project.resolution.label.take(6)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CineTextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Postproducción",
                    tint = CineEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
