package com.example.ui.avatar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AvatarProfile
import com.example.ui.theme.CineGold
import com.example.ui.theme.CineVoid
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-fidelity animated motion avatar view featuring:
 * - Real-time animated lip-sync simulation during speech
 * - Natural eye blinking animation
 * - Breathing and subtle head tilt motion
 * - Holographic neon soundwave border
 * - 100% Free badge
 */
@Composable
fun MotionAvatarView(
    avatar: AvatarProfile,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 140.dp,
    showFreeBadge: Boolean = true,
    showSpeakingWave: Boolean = true
) {
    // 1. Organic Breathing & Sway Animation
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_motion")

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )

    val headTiltDegrees by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "head_tilt"
    )

    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "aura_rotation"
    )

    // 2. Lip-Sync Mouth Opening (Animated dynamically when speaking)
    val mouthAnim by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = if (isSpeaking) 0.95f else 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 160 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouth_lipsync"
    )

    // 3. Eye Blink Simulation (Blinks every ~3 seconds)
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            val delayMillis = Random.nextLong(2500, 4200)
            kotlinx.coroutines.delay(delayMillis)
            isBlinking = true
            kotlinx.coroutines.delay(120) // blink closed
            isBlinking = false
        }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .rotate(headTiltDegrees)
            .scale(breathingScale),
        contentAlignment = Alignment.Center
    ) {
        // Holographic Rotating Neon Glow Ring
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(auraRotation)
        ) {
            val strokeWidth = 3.dp.toPx()
            val accent = Color(avatar.accentColor)
            val brush = Brush.sweepGradient(
                colors = listOf(
                    accent.copy(alpha = 0.9f),
                    CineGold.copy(alpha = 0.8f),
                    accent.copy(alpha = 0.2f),
                    accent.copy(alpha = 0.9f)
                )
            )
            drawCircle(
                brush = brush,
                radius = (size.minDimension / 2f) - strokeWidth,
                style = Stroke(width = strokeWidth)
            )
        }

        // Avatar Portrait Base
        Box(
            modifier = Modifier
                .fillMaxSize(0.88f)
                .clip(CircleShape)
                .background(CineVoid),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = avatar.imageRes),
                contentDescription = avatar.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Animated Eye Blinking Mask overlay
            if (isBlinking) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    // Left and right eye eyelid lids
                    drawArc(
                        color = Color(0xDD10121A),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.30f, h * 0.38f),
                        size = Size(w * 0.16f, h * 0.08f)
                    )
                    drawArc(
                        color = Color(0xDD10121A),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(w * 0.54f, h * 0.38f),
                        size = Size(w * 0.16f, h * 0.08f)
                    )
                }
            }

            // Real-Time Animated Mouth Lip-Sync Mask
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val mouthW = w * 0.18f
                val mouthH = (h * 0.14f * mouthAnim).coerceAtLeast(3.dp.toPx())
                val mouthCenter = Offset(w * 0.50f, h * 0.69f)

                // Render subtle lip contours and mouth cavity opening
                if (isSpeaking && mouthAnim > 0.25f) {
                    // Mouth cavity dark inside
                    drawOval(
                        color = Color(0xEE2A1015),
                        topLeft = Offset(mouthCenter.x - mouthW / 2f, mouthCenter.y - mouthH / 2f),
                        size = Size(mouthW, mouthH)
                    )
                    // Lip glow highlight
                    drawOval(
                        color = Color(avatar.accentColor).copy(alpha = 0.35f),
                        topLeft = Offset(mouthCenter.x - mouthW / 2f, mouthCenter.y - mouthH / 2f),
                        size = Size(mouthW, mouthH),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }

            // Speaking Soundwave Indicator overlay (bottom of portrait)
            if (isSpeaking && showSpeakingWave) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .background(Color(0xBB08090D), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(4) { i ->
                        val barHeight = (4 + ((sin(mouthAnim * 10f + i) + 1f) * 6)).dp
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(barHeight)
                                .background(Color(avatar.accentColor), CircleShape)
                        )
                    }
                }
            }
        }

        // "100% GRATIS" Cinematic Badge
        if (showFreeBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 8.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(CineGold, Color(0xFFFFA500))
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "100% GRATIS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = CineVoid
                )
            }
        }
    }
}
