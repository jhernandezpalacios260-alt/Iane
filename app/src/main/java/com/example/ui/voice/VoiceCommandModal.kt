package com.example.ui.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.VoiceCommandResult
import com.example.ui.theme.*

@Composable
fun VoiceCommandModal(
    isListening: Boolean,
    liveSpokenText: String,
    audioRms: Float,
    lastResult: VoiceCommandResult?,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSubmitCommand: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customTextInput by remember { mutableStateOf("") }

    val presetCommands = listOf(
        "Corta a 3 segundos y pon cámara lenta",
        "Añade destello anamórfico e iluminación dorada",
        "Añade avatar Sophia en la esquina derecha narrando",
        "Cambia la colorimetría a Blade Runner Neón",
        "Movimiento de dron orbital 360 grados",
        "Transición fundido a negro y render en 4K"
    )

    // Pulse animation for mic listening state
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("voice_command_modal"),
            colors = CardDefaults.cardColors(containerColor = CineSurface),
            shape = RoundedCornerShape(24.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(CineCyan, CineGold)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Voz",
                            tint = CineCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "DIRECTOR DE VOZ IA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CineTextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = CineTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Microphone Audio Visualizer Sphere
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer pulse ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(
                                        if (isListening) CineCyan.copy(alpha = 0.4f) else CineGold.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Inner Mic Action Button
                    FilledIconButton(
                        onClick = {
                            if (isListening) onStopListening() else onStartListening()
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("modal_mic_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isListening) CineCyan else CineGold,
                            contentColor = CineVoid
                        )
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = "Micrófono",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Prompt
                Text(
                    text = if (isListening) "Escuchando tu comando cinematográfico..." else "Pulsa el micrófono o selecciona una orden",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isListening) CineCyan else CineTextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                // Live recognized text preview
                if (liveSpokenText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CineSurfaceElevated, RoundedCornerShape(12.dp))
                            .border(1.dp, CineCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "\"$liveSpokenText\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CineCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Last applied edits breakdown
                if (lastResult != null && lastResult.changesApplied.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CineVoid, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "✓ SECUENCIA MODIFICADA:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CineEmerald
                        )
                        lastResult.changesApplied.forEach { change ->
                            Text(
                                text = "• $change",
                                style = MaterialTheme.typography.bodySmall,
                                color = CineTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick preset command chips
                Text(
                    text = "COMANDOS RÁPIDOS SUGERIDOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = CineTextMuted,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetCommands) { cmd ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CineSurfaceElevated,
                            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CineBorder, CineCyan.copy(alpha = 0.3f)))),
                            modifier = Modifier.clickable {
                                onSubmitCommand(cmd)
                            }
                        ) {
                            Text(
                                text = cmd,
                                fontSize = 11.sp,
                                color = CineTextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual text input option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customTextInput,
                        onValueChange = { customTextInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voice_command_input"),
                        placeholder = {
                            Text("Escribe un comando por texto...", fontSize = 12.sp, color = CineTextMuted)
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = CineTextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CineGold,
                            unfocusedBorderColor = CineBorder,
                            cursorColor = CineGold
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    FilledIconButton(
                        onClick = {
                            if (customTextInput.isNotBlank()) {
                                onSubmitCommand(customTextInput)
                                customTextInput = ""
                            }
                        },
                        enabled = customTextInput.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = CineGold,
                            contentColor = CineVoid
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Enviar")
                    }
                }
            }
        }
    }
}
