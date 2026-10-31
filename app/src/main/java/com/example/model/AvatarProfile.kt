package com.example.model

import com.example.R

enum class AvatarPlacement(val label: String) {
    BOTTOM_RIGHT("Esquina Inferior Derecha"),
    BOTTOM_LEFT("Esquina Inferior Izquierda"),
    PICTURE_IN_PICTURE("Ventana PIP Flotante"),
    CENTER_STAGE("Primer Plano Central")
}

data class AvatarProfile(
    val id: String,
    val name: String,
    val roleTitle: String,
    val imageRes: Int,
    val voiceTone: String,
    val bio: String,
    val accentColor: Long,
    val isFree: Boolean = true
) {
    companion object {
        val FREE_AVATARS = listOf(
            AvatarProfile(
                id = "sophia",
                name = "Sophia",
                roleTitle = "Directora de Cine IA",
                imageRes = R.drawable.img_avatar_sophia,
                voiceTone = "Elegante, segura y cinemática",
                bio = "Especialista en dirección dramática y ambientaciones sci-fi con dicción nítida.",
                accentColor = 0xFF00F0FF,
                isFree = true
            ),
            AvatarProfile(
                id = "leo",
                name = "Leo",
                roleTitle = "Narrador & Host",
                imageRes = R.drawable.img_avatar_leo,
                voiceTone = "Enérgico, carismático y dinámico",
                bio = "Ideal para secuencias de acción, trailers cinemáticos y relatos futuristas.",
                accentColor = 0xFFFFB800,
                isFree = true
            ),
            AvatarProfile(
                id = "kira",
                name = "Kira",
                roleTitle = "Holograma Sintético",
                imageRes = R.drawable.img_avatar_sophia,
                voiceTone = "Frecuencia etérea con eco estéreo",
                bio = "Aura holográfica y modulación futurista para cine espacial.",
                accentColor = 0xFF9D4EDD,
                isFree = true
            ),
            AvatarProfile(
                id = "marcus",
                name = "Marcus",
                roleTitle = "Voz Documental Épica",
                imageRes = R.drawable.img_avatar_leo,
                voiceTone = "Barítono profundo y solemne",
                bio = "Inspirado en narradores de cine clásico, historia y naturaleza salvaje.",
                accentColor = 0xFF05FFA1,
                isFree = true
            )
        )
    }
}

data class AvatarTrackConfig(
    val isEnabled: Boolean = true,
    val selectedAvatarId: String = "sophia",
    val placement: AvatarPlacement = AvatarPlacement.BOTTOM_RIGHT,
    val scaleFraction: Float = 0.35f,
    val scriptText: String = "Bienvenidos a la producción. Observen el destello anamórfico y la escala de la metrópoli.",
    val showBorderGlow: Boolean = true,
    val speechSpeed: Float = 1.0f,
    val voicePitch: Float = 1.0f
)
