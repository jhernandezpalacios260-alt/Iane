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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avatar.TtsController
import com.example.model.*
import com.example.ui.avatar.MotionAvatarView
import com.example.ui.theme.*

@Composable
fun AvatarStudioScreen(
    currentAvatarConfig: AvatarTrackConfig,
    ttsController: TtsController,
    onUpdateAvatarConfig: (AvatarTrackConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpeaking by ttsController.isSpeaking.collectAsState()
    var scriptInput by remember(currentAvatarConfig.scriptText) { mutableStateOf(currentAvatarConfig.scriptText) }

    val activeAvatar = AvatarProfile.FREE_AVATARS.find { it.id == currentAvatarConfig.selectedAvatarId }
        ?: AvatarProfile.FREE_AVATARS.first()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CineVoid)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 100% Free Guarantee Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CineSurfaceElevated),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CineGold, CineCyan)))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(CineGold.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = "Gratis", tint = CineGold, modifier = Modifier.size(28.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "AVATARES EN MOVIMIENTO",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CineTextPrimary
                            )
                            Text(
                                text = "100% GRATIS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CineVoid,
                                modifier = Modifier
                                    .background(CineGold, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = "Animación facial, sincronización labial (lip-sync), parpadeo y locución sin límites ni marcas de agua.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CineTextSecondary
                        )
                    }
                }
            }
        }

        // Live Avatar Motion Stage
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CineSurface),
                shape = RoundedCornerShape(20.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(CineBorder, CineVoid)))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = activeAvatar.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = CineTextPrimary
                            )
                            Text(
                                text = activeAvatar.roleTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(activeAvatar.accentColor)
                            )
                        }

                        // Toggle enable/disable
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (currentAvatarConfig.isEnabled) "Activo en Video" else "Oculto",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentAvatarConfig.isEnabled) CineEmerald else CineTextMuted
                            )
                            Switch(
                                checked = currentAvatarConfig.isEnabled,
                                onCheckedChange = { onUpdateAvatarConfig(currentAvatarConfig.copy(isEnabled = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CineVoid,
                                    checkedTrackColor = CineEmerald,
                                    uncheckedThumbColor = CineTextMuted,
                                    uncheckedTrackColor = CineSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Live Animated Avatar
                    MotionAvatarView(
                        avatar = activeAvatar,
                        isSpeaking = isSpeaking,
                        sizeDp = 150.dp,
                        showFreeBadge = true,
                        showSpeakingWave = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isSpeaking) "🎙️ Locutando en tiempo real con lip-sync sincronizado..." else "En reposo orgánico: parpadeo y respiración activos",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSpeaking) CineCyan else CineTextMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test Voice TTS Button
                    Button(
                        onClick = {
                            if (isSpeaking) {
                                ttsController.stop()
                            } else {
                                ttsController.speak(
                                    text = scriptInput,
                                    pitch = currentAvatarConfig.voicePitch,
                                    speed = currentAvatarConfig.speechSpeed
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpeaking) CineNeonRed else CineCyan,
                            contentColor = CineVoid
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("test_avatar_speech_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = "Probar Voz",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSpeaking) "Detener Voz" else "Probar Voz & Lip-Sync",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Script Editor
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CineSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(CineBorder, CineVoid)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "GUIÓN DE LOCUCIÓN DEL AVATAR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CineGold
                        )
                        Text(
                            text = "Voz Sintetizada",
                            fontSize = 10.sp,
                            color = CineCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = scriptInput,
                        onValueChange = {
                            scriptInput = it
                            onUpdateAvatarConfig(currentAvatarConfig.copy(scriptText = it))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("avatar_script_input"),
                        minLines = 2,
                        maxLines = 4,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = CineTextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CineGold,
                            unfocusedBorderColor = CineBorder,
                            cursorColor = CineGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset speech scripts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TextButton(
                            onClick = {
                                val s = "Bienvenidos al metraje cinematográfico. Todos los sistemas visuales operan en alta resolución 4K."
                                scriptInput = s
                                onUpdateAvatarConfig(currentAvatarConfig.copy(scriptText = s))
                            }
                        ) {
                            Text("Intro Sci-Fi", fontSize = 11.sp, color = CineGold)
                        }

                        TextButton(
                            onClick = {
                                val s = "La atmósfera dorada y el grano de 35 milímetros aportan un acabado digno de la gran pantalla."
                                scriptInput = s
                                onUpdateAvatarConfig(currentAvatarConfig.copy(scriptText = s))
                            }
                        ) {
                            Text("Comentario Pro", fontSize = 11.sp, color = CineCyan)
                        }
                    }
                }
            }
        }

        // Avatar Selection Gallery
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "ELENCO DE AVATARES DISPONIBLES (TODOS GRATIS)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CineTextSecondary
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(AvatarProfile.FREE_AVATARS) { av ->
                        val isSelected = av.id == currentAvatarConfig.selectedAvatarId
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) CineSurfaceElevated else CineSurfaceVariant
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CineGold, CineCyan))) else CardDefaults.outlinedCardBorder(),
                            modifier = Modifier
                                .width(135.dp)
                                .clickable {
                                    onUpdateAvatarConfig(currentAvatarConfig.copy(selectedAvatarId = av.id))
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                MotionAvatarView(
                                    avatar = av,
                                    isSpeaking = false,
                                    sizeDp = 64.dp,
                                    showFreeBadge = false,
                                    showSpeakingWave = false
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = av.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CineGold else CineTextPrimary
                                )
                                Text(
                                    text = av.roleTitle,
                                    fontSize = 9.sp,
                                    color = CineTextMuted,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Placement & Scale Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CineSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "POSICIÓN EN LA PANTALLA DE VIDEO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CineTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AvatarPlacement.entries.forEach { p ->
                            val isSel = p == currentAvatarConfig.placement
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) CineCyan else CineSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onUpdateAvatarConfig(currentAvatarConfig.copy(placement = p)) }
                            ) {
                                Text(
                                    text = when (p) {
                                        AvatarPlacement.BOTTOM_RIGHT -> "Abajo Der"
                                        AvatarPlacement.BOTTOM_LEFT -> "Abajo Izq"
                                        AvatarPlacement.PICTURE_IN_PICTURE -> "PIP"
                                        AvatarPlacement.CENTER_STAGE -> "Centro"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) CineVoid else CineTextPrimary,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Scale Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tamaño del Avatar: ${(currentAvatarConfig.scaleFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = CineTextPrimary
                        )
                    }
                    Slider(
                        value = currentAvatarConfig.scaleFraction,
                        onValueChange = { onUpdateAvatarConfig(currentAvatarConfig.copy(scaleFraction = it)) },
                        valueRange = 0.22f..0.55f,
                        colors = SliderDefaults.colors(thumbColor = CineCyan, activeTrackColor = CineCyan)
                    )
                }
            }
        }
    }
}
