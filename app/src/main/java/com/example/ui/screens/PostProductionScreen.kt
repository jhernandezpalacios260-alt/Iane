package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PostProductionScreen(
    currentProject: CinematicProject,
    onUpdateProject: (CinematicProject) -> Unit,
    modifier: Modifier = Modifier
) {
    var showExportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }
    var exportDone by remember { mutableStateOf(false) }

    LaunchedEffect(isExporting) {
        if (isExporting) {
            exportProgress = 0f
            exportDone = false
            while (exportProgress < 1f) {
                delay(120)
                exportProgress += 0.04f
            }
            exportProgress = 1f
            exportDone = true
            isExporting = false
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CineVoid)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "POSTPRODUCCIÓN AUTOMATIZADA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CineTextPrimary
                    )
                    Text(
                        text = "Colorimetría LUT, Audio Foley & Masterización 4K",
                        style = MaterialTheme.typography.bodySmall,
                        color = CineTextSecondary
                    )
                }

                Button(
                    onClick = { showExportDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CineGold, contentColor = CineVoid),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_export_modal_button")
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = "Exportar", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Render 4K", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section 1: Auto Color Grading (LUTs)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CineSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(CineBorder, CineVoid)))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Color", tint = CineGold)
                        Text(
                            text = "COLORIMETRÍA CINEMATOGRÁFICA (LUTs)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CineGold
                        )
                    }

                    Text(
                        text = "Tablas de color aplicadas en tiempo real a todas las tomas del montaje.",
                        style = MaterialTheme.typography.bodySmall,
                        color = CineTextSecondary
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(ColorGrade.entries) { lut ->
                            val isSelected = lut == currentProject.colorGrade
                            val tint = Color(lut.tintColor)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) CineSurfaceElevated else CineSurfaceVariant,
                                border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CineGold, tint))) else CardDefaults.outlinedCardBorder(),
                                modifier = Modifier
                                    .width(135.dp)
                                    .clickable { onUpdateProject(currentProject.copy(colorGrade = lut)) }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    // Color swatch preview
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(28.dp)
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    listOf(tint.copy(alpha = 0.8f), CineVoid)
                                                ),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = lut.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) CineGold else CineTextPrimary,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = lut.description,
                                        fontSize = 9.sp,
                                        color = CineTextMuted,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Automated Audio & Foley Soundscapes
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CineSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = "Audio", tint = CineCyan)
                        Text(
                            text = "DISEÑO SONORO & BANDA SONORA IA",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = CineCyan
                        )
                    }

                    // Soundtrack theme picker
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SoundtrackTheme.entries) { st ->
                            val isSel = st == currentProject.audioConfig.soundtrack
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) CineCyan else CineSurfaceVariant,
                                modifier = Modifier.clickable {
                                    onUpdateProject(
                                        currentProject.copy(
                                            audioConfig = currentProject.audioConfig.copy(soundtrack = st)
                                        )
                                    )
                                }
                            ) {
                                Text(
                                    text = st.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) CineVoid else CineTextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Foley switches
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Realce de Bajos & Drones Subgraves", style = MaterialTheme.typography.bodyMedium, color = CineTextPrimary)
                            Text(text = "Impacto cinemático en trailers de acción", style = MaterialTheme.typography.bodySmall, color = CineTextMuted)
                        }
                        Switch(
                            checked = currentProject.audioConfig.bassDroneBoost,
                            onCheckedChange = {
                                onUpdateProject(currentProject.copy(audioConfig = currentProject.audioConfig.copy(bassDroneBoost = it)))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CineVoid, checkedTrackColor = CineCyan)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Golpes & Risers de Trailer", style = MaterialTheme.typography.bodyMedium, color = CineTextPrimary)
                            Text(text = "Transiciones sonoras automáticas al corte", style = MaterialTheme.typography.bodySmall, color = CineTextMuted)
                        }
                        Switch(
                            checked = currentProject.audioConfig.cinematicHits,
                            onCheckedChange = {
                                onUpdateProject(currentProject.copy(audioConfig = currentProject.audioConfig.copy(cinematicHits = it)))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CineVoid, checkedTrackColor = CineCyan)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Foley Ambiental de Lluvia y Clima", style = MaterialTheme.typography.bodyMedium, color = CineTextPrimary)
                            Text(text = "Atmósfera inmersiva para secuencias nocturnas", style = MaterialTheme.typography.bodySmall, color = CineTextMuted)
                        }
                        Switch(
                            checked = currentProject.audioConfig.rainFoley,
                            onCheckedChange = {
                                onUpdateProject(currentProject.copy(audioConfig = currentProject.audioConfig.copy(rainFoley = it)))
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CineVoid, checkedTrackColor = CineCyan)
                        )
                    }
                }
            }
        }

        // Section 3: Aspect Ratio & Lens Framing
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CineSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "FORMATO DE PANTALLA & ASPECT RATIO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CineTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AspectRatio.entries.forEach { ar ->
                            val isSel = ar == currentProject.aspectRatio
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) CineGold else CineSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateProject(currentProject.copy(aspectRatio = ar)) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = ar.badge,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) CineVoid else CineTextPrimary
                                    )
                                    Text(
                                        text = ar.label.substringBefore(" "),
                                        fontSize = 10.sp,
                                        color = if (isSel) CineVoid else CineTextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // High Resolution 4K Export Engine Modal
    if (showExportDialog) {
        Dialog(onDismissRequest = { if (!isExporting) showExportDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CineSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(CineGold, CineCyan))),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag("export_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "EXPORTACIÓN MASTER 4K",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CineGold
                        )
                        if (!isExporting) {
                            IconButton(onClick = { showExportDialog = false }, modifier = Modifier.size(28.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = CineTextSecondary)
                            }
                        }
                    }

                    if (exportDone) {
                        // Success View
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(CineEmerald.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Éxito", tint = CineEmerald, modifier = Modifier.size(40.dp))
                        }

                        Text(
                            text = "¡Video Renderizado con Éxito!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CineTextPrimary
                        )
                        Text(
                            text = "Master codificado en ${currentProject.resolution.pixelDimensions} a 60 FPS • ${currentProject.resolution.bitrate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CineTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Button(
                            onClick = {
                                exportDone = false
                                showExportDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CineGold, contentColor = CineVoid),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Aceptar & Ver en Reproductor", fontWeight = FontWeight.Bold)
                        }
                    } else if (isExporting) {
                        // Progress View
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(CineCyan.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { exportProgress },
                                color = CineCyan,
                                modifier = Modifier.size(50.dp)
                            )
                        }

                        Text(
                            text = "Renderizando Frames en Alta Resolución...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = CineTextPrimary
                        )
                        LinearProgressIndicator(
                            progress = { exportProgress },
                            color = CineGold,
                            trackColor = CineSurfaceElevated,
                            modifier = Modifier.fillMaxWidth().height(6.dp)
                        )
                        Text(
                            text = "${(exportProgress * 100).toInt()}% • Procesando efectos de cámara y lip-sync...",
                            fontSize = 11.sp,
                            color = CineTextSecondary
                        )
                    } else {
                        // Options View
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "RESOLUCIÓN DE SALIDA",
                                style = MaterialTheme.typography.labelSmall,
                                color = CineTextSecondary
                            )
                            ExportResolution.entries.forEach { res ->
                                val isSel = res == currentProject.resolution
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSel) CineSurfaceElevated else CineSurfaceVariant,
                                    border = if (isSel) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CineGold, CineCyan))) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onUpdateProject(currentProject.copy(resolution = res)) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(text = res.label, fontWeight = FontWeight.Bold, color = if (isSel) CineGold else CineTextPrimary)
                                            Text(text = "${res.pixelDimensions} • ${res.bitrate}", fontSize = 11.sp, color = CineTextMuted)
                                        }
                                        if (isSel) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = "Seleccionado", tint = CineGold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { isExporting = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_render_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CineGold, contentColor = CineVoid)
                        ) {
                            Icon(imageVector = Icons.Default.PlayCircle, contentDescription = "Iniciar")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("INICIAR PROCESAMIENTO 4K", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
