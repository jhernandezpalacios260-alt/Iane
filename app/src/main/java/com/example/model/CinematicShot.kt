package com.example.model

enum class CameraMotion(val label: String, val iconDescription: String) {
    PAN_LEFT("Panorámica Izquierda", "Movimiento lateral suave"),
    PAN_RIGHT("Panorámica Derecha", "Barrido cinematográfico"),
    DOLLY_IN("Dolly Zoom In", "Acercamiento dramático"),
    DOLLY_OUT("Dolly Zoom Out", "Alejamiento revelador"),
    ORBITAL_DRONE("Dron Orbital 360°", "Giro aéreo envolvente"),
    RACK_FOCUS("Rack Focus", "Desenfoque selectivo"),
    FPV_DIVE("FPV Dive", "Vuelo cinético vertiginoso"),
    STATIC_WIDE("Plano General Fijo", "Composición estática majestuosa")
}

enum class SpeedRamp(val multiplier: Float, val label: String) {
    SLO_MO_HALF(0.5f, "0.5x Slow-Mo"),
    STANDARD(1.0f, "1.0x Normal"),
    FAST_ACTION(1.5f, "1.5x Acción Rápida"),
    HYPERLAPSE(2.5f, "2.5x Hiperlapso")
}

enum class VisualFilter(val label: String, val description: String) {
    NONE("Natural", "Sin efectos ópticos añadidos"),
    VOLUMETRIC_FOG("Niebla Volumétrica", "Haces de luz y bruma atmosférica"),
    ANAMORPHIC_FLARE("Destello Anamórfico", "Línea horizontal cian y destello dorado"),
    FILM_GRAIN("Grano 35mm Real", "Textura analógica cinematográfica"),
    EMBER_PARTICLES("Partículas & Chispas", "Brasas incandescentes flotantes"),
    RAIN_MIST("Lluvia & Reflejos", "Gotas cinemáticas y neblina"),
    CYBER_GLOW("Aura Holográfica", "Contornos de luz ciberpunk")
}

enum class SceneTransition(val label: String) {
    CUT("Corte Directo"),
    CROSS_DISSOLVE("Disolvencia Cruzada"),
    FADE_TO_BLACK("Fundido a Negro"),
    WHIP_PAN("Barrido Rápido"),
    GLITCH_ZOOM("Zoom Glitch IA"),
    LIGHT_LEAK("Fuga de Luz Cálida")
}

data class CinematicShot(
    val id: String,
    val title: String,
    val promptDescription: String,
    val durationSeconds: Float,
    val cameraMotion: CameraMotion = CameraMotion.PAN_RIGHT,
    val speedRamp: SpeedRamp = SpeedRamp.STANDARD,
    val visualFilter: VisualFilter = VisualFilter.ANAMORPHIC_FLARE,
    val transition: SceneTransition = SceneTransition.CROSS_DISSOLVE,
    val imageDrawableRes: Int? = null,
    val narrationText: String = "",
    val lightingMood: String = "Cálida de atardecer",
    val focalLength: String = "35mm T1.5"
)
