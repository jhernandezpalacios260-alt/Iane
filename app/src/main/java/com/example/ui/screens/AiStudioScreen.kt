package com.example.ui.screens

import androidx.compose.foundation.Image
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
fun AiStudioScreen(
    currentProject: CinematicProject,
    onGenerateNewSequence: (prompt: String, genre: String, shotsCount: Int) -> Unit,
    onOpenEditorTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    var userPrompt by remember {
        mutableStateOf("Metrópoli futurista bajo lluvia con vehículos voladores y rascacielos holográficos en plano general cinemático")
    }
    var selectedGenre by remember { mutableStateOf("Cyberpunk 2099") }
    var selectedShotCount by remember { mutableIntStateOf(3) }
    var isGenerating by remember { mutableStateOf(false) }

    val genres = listOf(
        "Cyberpunk 2099",
        "Space Odyssey Sci-Fi",
        "Epic High Fantasy",
        "Neo-Noir Crime",
        "Documental IMAX",
        "Acción Cinética"
    )

    val cameraStyles = listOf(
        "Dolly Zoom Vértigo",
        "Dron Orbital 360°",
        "Anamórfico Wide 2.39:1",
        "FPV Dive Veloz",
        "Rack Focus Óptico"
    )
    var selectedCameraStyle by remember { mutableStateOf(cameraStyles.first()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CineVoid)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CineBorder, RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_cyberpunk),
                    contentDescription = "AI Cinema",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, CineVoid.copy(alpha = 0.95f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "IA GENERATIVA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CineGold,
                            modifier = Modifier
                                .background(CineGold.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "4K HDR READY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CineCyan,
                            modifier = Modifier
                                .background(CineCyan.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generador de Cine & Storyboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CineTextPrimary
                    )
                    Text(
                        text = "Crea tomas complejas con dirección de cámara y avatares parlantes",
                        style = MaterialTheme.typography.bodySmall,
                        color = CineTextSecondary
                    )
                }
            }
        }

        // Prompt Input
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CineSurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(CineBorder, CineVoid)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PROMPT CINEMATOGRÁFICO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CineGold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = userPrompt,
                        onValueChange = { userPrompt = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_prompt_input"),
                        minLines = 3,
                        maxLines = 5,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = CineTextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CineGold,
                            unfocusedBorderColor = CineBorder,
                            cursorColor = CineGold
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // AI Prompt Expander button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                userPrompt = "Nave espacial en órbita alrededor de un gigante gaseoso anillado con llamarada solar dorada, estilo Interestelar, anamórfico 2.39:1, niebla volumétrica"
                            }
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Sugerir", tint = CineCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sugerencia Sci-Fi", fontSize = 11.sp, color = CineCyan)
                        }

                        TextButton(
                            onClick = {
                                userPrompt = "Templo flotante milenario al atardecer sobre un mar de nubes doradas con runas místicas brillantes y cascadas hacia el vacío, estilo fantasía épica 8K"
                            }
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Sugerir", tint = CineGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sugerencia Fantasía", fontSize = 11.sp, color = CineGold)
                        }
                    }
                }
            }
        }

        // Genre Picker
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "GÉNERO & ESTILO CINEMATOGRÁFICO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CineTextSecondary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(genres) { g ->
                        val isSelected = g == selectedGenre
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CineGold else CineSurfaceVariant,
                            border = if (isSelected) null else CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.clickable { selectedGenre = g }
                        ) {
                            Text(
                                text = g,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CineVoid else CineTextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Camera Style
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "DIRECCIÓN DE CÁMARA PRINCIPAL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = CineTextSecondary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cameraStyles) { cam ->
                        val isSelected = cam == selectedCameraStyle
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CineCyan else CineSurfaceVariant,
                            border = if (isSelected) null else CardDefaults.outlinedCardBorder(),
                            modifier = Modifier.clickable { selectedCameraStyle = cam }
                        ) {
                            Text(
                                text = cam,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CineVoid else CineTextPrimary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Shots count selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CineSurface, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Cantidad de Tomas en la Secuencia",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CineTextPrimary
                    )
                    Text(
                        text = "Genera planos de corte, ritmo y variedad de ángulos",
                        style = MaterialTheme.typography.bodySmall,
                        color = CineTextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(3, 4, 5).forEach { count ->
                        val selected = selectedShotCount == count
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) CineGold else CineSurfaceVariant,
                            modifier = Modifier.clickable { selectedShotCount = count }
                        ) {
                            Text(
                                text = "$count Tomas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) CineVoid else CineTextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Generate Action Button
        item {
            Button(
                onClick = {
                    isGenerating = true
                    onGenerateNewSequence(userPrompt, selectedGenre, selectedShotCount)
                    onOpenEditorTab()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_sequence_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CineGold,
                    contentColor = CineVoid
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(imageVector = Icons.Default.MovieFilter, contentDescription = "Generar", modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GENERAR SECUENCIA CINEMÁTICA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
