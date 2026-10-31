package com.example.model

enum class AspectRatio(val ratioFloat: Float, val label: String, val badge: String) {
    CINEMASCOPE(2.39f, "CinemaScope 2.39:1", "CINE"),
    WIDESCREEN(16f / 9f, "Widescreen 16:9", "16:9"),
    VERTICAL_REEL(9f / 16f, "Vertical Reel 9:16", "9:16")
}

enum class ExportResolution(val label: String, val pixelDimensions: String, val bitrate: String) {
    UHD_4K("4K Ultra HD", "3840 x 2160", "85 Mbps (Master Pro)"),
    QHD_2K("2K Cinema QHD", "2560 x 1440", "45 Mbps"),
    FHD_1080P("1080p Full HD", "1920 x 1080", "20 Mbps")
}

enum class ColorGrade(val label: String, val description: String, val tintColor: Long) {
    TEAL_AND_ORANGE("Teal & Orange", "Contraste moderno de Hollywood", 0xFF00E5FF),
    BLADE_RUNNER("Blade Runner Neon", "Paleta ciberpunk cian y magenta", 0xFFFF2A6D),
    KODAK_35MM("Kodak Vision3 500T", "Calidez analógica clásica de 35mm", 0xFFFFB800),
    GOLDEN_HOUR("Golden Hour Glow", "Luz dorada cálida envolvente", 0xFFFF9E00),
    BLEACH_BYPASS("Bleach Bypass", "Desaturación dura y sombras profundas", 0xFFA0AEC0),
    NOIR_MONOCHROME("Noir Cinema B&W", "Blanco y negro de alto contraste", 0xFFE2E8F0)
}

enum class SoundtrackTheme(val label: String, val tempoBpm: Int, val description: String) {
    EPIC_ORCHESTRAL("Épico Orquestal & Bronces", 92, "Cuerdas majestuosas y golpes de trailer"),
    SYNTHWAVE_CYBER("Cyberpunk Synth Drones", 110, "Bajos analógicos profundos y arpegios"),
    DARK_MYSTERY("Misterio & Suspenso", 74, "Atmósferas sutiles y frecuencias subgraves"),
    DOCU_AMBIENCE("Atmósfera Documental", 84, "Pianos minimalistas y cuerdas de fondo"),
    ACTION_PULSE("Pulso de Acción Cinética", 128, "Percusiones híbridas y ritmo trepidante")
}

data class AudioPostConfig(
    val soundtrack: SoundtrackTheme = SoundtrackTheme.SYNTHWAVE_CYBER,
    val musicVolume: Float = 0.75f,
    val voiceoverVolume: Float = 1.0f,
    val rainFoley: Boolean = true,
    val bassDroneBoost: Boolean = true,
    val cinematicHits: Boolean = true
)

data class CinematicProject(
    val id: String,
    val title: String,
    val genre: String,
    val shots: List<CinematicShot>,
    val aspectRatio: AspectRatio = AspectRatio.CINEMASCOPE,
    val resolution: ExportResolution = ExportResolution.UHD_4K,
    val colorGrade: ColorGrade = ColorGrade.BLADE_RUNNER,
    val audioConfig: AudioPostConfig = AudioPostConfig(),
    val avatarConfig: AvatarTrackConfig = AvatarTrackConfig(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val totalDurationSeconds: Float
        get() = shots.sumOf { it.durationSeconds.toDouble() }.toFloat()
}
