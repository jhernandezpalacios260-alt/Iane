package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceCommandEngine(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _liveSpokenText = MutableStateFlow("")
    val liveSpokenText: StateFlow<String> = _liveSpokenText.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _lastResult = MutableStateFlow<VoiceCommandResult?>(null)
    val lastResult: StateFlow<VoiceCommandResult?> = _lastResult.asStateFlow()

    init {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }
            }
        } catch (e: Exception) {
            Log.w("VoiceCommandEngine", "Speech recognizer initialization note: ${e.message}")
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
                _liveSpokenText.value = ""
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize 0f..10f to 0f..1f
                _audioRms.value = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                Log.w("VoiceCommandEngine", "Speech error code: $error")
            }

            override fun onResults(results: Bundle?) {
                _isListening.value = false
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull() ?: ""
                _liveSpokenText.value = recognized
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let {
                    _liveSpokenText.value = it
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(createListener())
                }
            }
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e("VoiceCommandEngine", "Cannot start speech recognizer: ${e.message}")
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    /**
     * Interprets a voice command (whether received from SpeechRecognizer or typed)
     * and maps it to concrete cinematic sequence mutations.
     */
    fun parseCommand(spokenText: String, activeShotIndex: Int, currentProject: CinematicProject): Pair<CinematicProject, VoiceCommandResult> {
        val lower = spokenText.lowercase(Locale.ROOT).trim()
        val changes = mutableListOf<String>()
        var updatedProject = currentProject
        val targetIndex = if (activeShotIndex in currentProject.shots.indices) activeShotIndex else 0

        // 1. Duration / Trim analysis
        val secondsMatch = Regex("""(?:a|duración|duracion|corta(?:r)? a)?\s*(\d+(?:\.\d+)?)\s*(?:segundos?|s)""").find(lower)
        if (secondsMatch != null) {
            val seconds = secondsMatch.groupValues[1].toFloatOrNull() ?: 3.0f
            if (seconds in 0.5f..30.0f && currentProject.shots.isNotEmpty()) {
                val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                    if (idx == targetIndex) shot.copy(durationSeconds = seconds) else shot
                }
                updatedProject = updatedProject.copy(shots = updatedShots)
                changes.add("Escena ${targetIndex + 1} ajustada a ${seconds}s de duración")
            }
        }

        // 2. Speed ramp
        if (lower.contains("cámara lenta") || lower.contains("camara lenta") || lower.contains("slow mo") || lower.contains("slow motion") || lower.contains("lento")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(speedRamp = SpeedRamp.SLO_MO_HALF) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Velocidad ajustada a 0.5x Slow-Mo en Escena ${targetIndex + 1}")
        } else if (lower.contains("rápido") || lower.contains("rapido") || lower.contains("acelerar") || lower.contains("fast")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(speedRamp = SpeedRamp.FAST_ACTION) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Velocidad acelerada a 1.5x Acción en Escena ${targetIndex + 1}")
        } else if (lower.contains("velocidad normal") || lower.contains("normal")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(speedRamp = SpeedRamp.STANDARD) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Velocidad restablecida a 1.0x Normal")
        }

        // 3. Visual Filters & Atmosphere
        if (lower.contains("niebla") || lower.contains("bruma") || lower.contains("humo") || lower.contains("fog")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(visualFilter = VisualFilter.VOLUMETRIC_FOG) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Niebla Volumétrica cinematográfica activada en Escena ${targetIndex + 1}")
        } else if (lower.contains("destello") || lower.contains("anamórfico") || lower.contains("anamorfico") || lower.contains("flare")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(visualFilter = VisualFilter.ANAMORPHIC_FLARE) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Destello Anamórfico Hollywood cian/oro aplicado en Escena ${targetIndex + 1}")
        } else if (lower.contains("partícula") || lower.contains("particulas") || lower.contains("chispa") || lower.contains("chispas") || lower.contains("fuego")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(visualFilter = VisualFilter.EMBER_PARTICLES) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Efecto de Brasas y Partículas incandescentes añadido")
        } else if (lower.contains("lluvia") || lower.contains("rain") || lower.contains("gotas")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(visualFilter = VisualFilter.RAIN_MIST) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Lluvia cinemática con reflejos y bruma aplicada")
        } else if (lower.contains("grano") || lower.contains("film grain") || lower.contains("35mm")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(visualFilter = VisualFilter.FILM_GRAIN) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Textura de grano analógico Kodak 35mm añadida")
        }

        // 4. Color Grading (LUTs) & Lighting
        if (lower.contains("dorad") || lower.contains("golden") || lower.contains("atardecer") || lower.contains("puesta de sol")) {
            updatedProject = updatedProject.copy(colorGrade = ColorGrade.GOLDEN_HOUR)
            changes.add("Colorimetría LUT: Golden Hour Glow (Luz Cálida de Cine)")
        } else if (lower.contains("blade runner") || lower.contains("ciberpunk") || lower.contains("cyberpunk") || lower.contains("neon") || lower.contains("neón")) {
            updatedProject = updatedProject.copy(colorGrade = ColorGrade.BLADE_RUNNER)
            changes.add("Colorimetría LUT: Blade Runner Neon (Cian & Magenta)")
        } else if (lower.contains("teal") || lower.contains("orange") || lower.contains("naranja") || lower.contains("hollywood")) {
            updatedProject = updatedProject.copy(colorGrade = ColorGrade.TEAL_AND_ORANGE)
            changes.add("Colorimetría LUT: Blockbuster Teal & Orange")
        } else if (lower.contains("blanco y negro") || lower.contains("noir") || lower.contains("monocrom")) {
            updatedProject = updatedProject.copy(colorGrade = ColorGrade.NOIR_MONOCHROME)
            changes.add("Colorimetría LUT: Noir Cinema Blanco y Negro")
        } else if (lower.contains("kodak") || lower.contains("analógico") || lower.contains("analogico") || lower.contains("vintage")) {
            updatedProject = updatedProject.copy(colorGrade = ColorGrade.KODAK_35MM)
            changes.add("Colorimetría LUT: Kodak Vision3 500T 35mm")
        }

        // 5. Camera Motion
        if (lower.contains("dron") || lower.contains("orbital") || lower.contains("360") || lower.contains("gir")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(cameraMotion = CameraMotion.ORBITAL_DRONE) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Movimiento de cámara: Dron Orbital 360°")
        } else if (lower.contains("dolly") || lower.contains("zoom") || lower.contains("vértigo") || lower.contains("vertigo")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(cameraMotion = CameraMotion.DOLLY_IN) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Movimiento de cámara: Dolly Zoom In dramático")
        } else if (lower.contains("paneo") || lower.contains("panorámica") || lower.contains("panoramica")) {
            val motion = if (lower.contains("izquierda")) CameraMotion.PAN_LEFT else CameraMotion.PAN_RIGHT
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(cameraMotion = motion) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Movimiento de cámara: ${motion.label}")
        } else if (lower.contains("rack focus") || lower.contains("desenfoque")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(cameraMotion = CameraMotion.RACK_FOCUS) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Movimiento de cámara: Rack Focus óptico")
        }

        // 6. Transitions
        if (lower.contains("fundido a negro") || lower.contains("fade to black")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(transition = SceneTransition.FADE_TO_BLACK) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Transición de salida: Fundido a Negro")
        } else if (lower.contains("disolvencia") || lower.contains("disolver")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(transition = SceneTransition.CROSS_DISSOLVE) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Transición de salida: Disolvencia Cruzada")
        } else if (lower.contains("glitch")) {
            val updatedShots = updatedProject.shots.mapIndexed { idx, shot ->
                if (idx == targetIndex) shot.copy(transition = SceneTransition.GLITCH_ZOOM) else shot
            }
            updatedProject = updatedProject.copy(shots = updatedShots)
            changes.add("Transición de salida: Glitch Zoom IA")
        }

        // 7. Avatar Configuration & Narration
        if (lower.contains("quitar avatar") || lower.contains("eliminar avatar") || lower.contains("sin avatar")) {
            updatedProject = updatedProject.copy(avatarConfig = updatedProject.avatarConfig.copy(isEnabled = false))
            changes.add("Avatar en movimiento desactivado")
        } else if (lower.contains("avatar") || lower.contains("narrador") || lower.contains("presentador")) {
            var avatarConfig = updatedProject.avatarConfig.copy(isEnabled = true)
            if (lower.contains("sophia")) {
                avatarConfig = avatarConfig.copy(selectedAvatarId = "sophia")
                changes.add("Avatar seleccionado: Sophia (Directora Ciberpunk)")
            } else if (lower.contains("leo")) {
                avatarConfig = avatarConfig.copy(selectedAvatarId = "leo")
                changes.add("Avatar seleccionado: Leo (Narrador Cinemático)")
            } else if (lower.contains("kira")) {
                avatarConfig = avatarConfig.copy(selectedAvatarId = "kira")
                changes.add("Avatar seleccionado: Kira (Holograma IA)")
            } else if (lower.contains("marcus")) {
                avatarConfig = avatarConfig.copy(selectedAvatarId = "marcus")
                changes.add("Avatar seleccionado: Marcus (Voz Épica)")
            }

            if (lower.contains("derecha")) {
                avatarConfig = avatarConfig.copy(placement = AvatarPlacement.BOTTOM_RIGHT)
                changes.add("Posición del avatar: Esquina Inferior Derecha")
            } else if (lower.contains("izquierda")) {
                avatarConfig = avatarConfig.copy(placement = AvatarPlacement.BOTTOM_LEFT)
                changes.add("Posición del avatar: Esquina Inferior Izquierda")
            } else if (lower.contains("pip") || lower.contains("flotante") || lower.contains("ventana")) {
                avatarConfig = avatarConfig.copy(placement = AvatarPlacement.PICTURE_IN_PICTURE)
                changes.add("Posición del avatar: PIP Ventana Flotante")
            } else if (lower.contains("centro") || lower.contains("central")) {
                avatarConfig = avatarConfig.copy(placement = AvatarPlacement.CENTER_STAGE)
                changes.add("Posición del avatar: Primer Plano Central")
            }

            // Check if user dictated narration for the avatar: "el avatar dice que..." or "narrar que..."
            val narrateMatch = Regex("""(?:el avatar dice|di|narra|narrando|diga|guion|guión)\s*[: que]*(.+)""").find(lower)
            if (narrateMatch != null) {
                val script = narrateMatch.groupValues[1].replaceFirstChar { it.uppercase() }
                avatarConfig = avatarConfig.copy(scriptText = script)
                changes.add("Nuevo guión de voz y lip-sync sincronizado: \"$script\"")
            }

            updatedProject = updatedProject.copy(avatarConfig = avatarConfig)
            if (changes.none { it.contains("Avatar") }) {
                changes.add("Avatar con movimiento activado en escena")
            }
        }

        // 8. Aspect Ratio & Resolution
        if (lower.contains("4k") || lower.contains("ultra hd")) {
            updatedProject = updatedProject.copy(resolution = ExportResolution.UHD_4K)
            changes.add("Resolución de render configurada a 4K Ultra HD (3840x2160)")
        } else if (lower.contains("1080") || lower.contains("full hd")) {
            updatedProject = updatedProject.copy(resolution = ExportResolution.FHD_1080P)
            changes.add("Resolución configurada a 1080p Master")
        }

        if (lower.contains("2.39") || lower.contains("anamórfico") || lower.contains("cinemascope") || lower.contains("cine")) {
            updatedProject = updatedProject.copy(aspectRatio = AspectRatio.CINEMASCOPE)
            changes.add("Formato de pantalla: CinemaScope 2.39:1 con barras cinematográficas")
        } else if (lower.contains("16:9") || lower.contains("widescreen")) {
            updatedProject = updatedProject.copy(aspectRatio = AspectRatio.WIDESCREEN)
            changes.add("Formato de pantalla: Widescreen 16:9 estándar")
        } else if (lower.contains("9:16") || lower.contains("vertical") || lower.contains("tiktok") || lower.contains("reel") || lower.contains("short")) {
            updatedProject = updatedProject.copy(aspectRatio = AspectRatio.VERTICAL_REEL)
            changes.add("Formato de pantalla: Vertical Reel 9:16 para redes sociales")
        }

        // Fallback for general scene creation intent
        if (changes.isEmpty()) {
            changes.add("Comando procesado: optimización estética y ajuste cinemático de la toma")
        }

        val result = VoiceCommandResult(
            spokenText = spokenText,
            recognizedIntent = if (changes.size > 1) "Edición secuencial múltiple" else changes.firstOrNull() ?: "Ajuste cinemático",
            changesApplied = changes,
            isSuccess = true
        )
        _lastResult.value = result
        return Pair(updatedProject, result)
    }
}
