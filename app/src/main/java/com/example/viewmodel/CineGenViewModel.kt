package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.avatar.TtsController
import com.example.model.*
import com.example.voice.VoiceCommandEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CineGenViewModel(application: Application) : AndroidViewModel(application) {

    val voiceEngine = VoiceCommandEngine(application)
    val ttsController = TtsController(application)

    private val initialShots = listOf(
        CinematicShot(
            id = "shot_1",
            title = "Amanecer Neón en Neo-Tokio",
            promptDescription = "Plano general anamórfico de la metrópoli ciberpunk bajo la lluvia con vehículos aéreos y reflejos de neón",
            durationSeconds = 4.0f,
            cameraMotion = CameraMotion.PAN_RIGHT,
            speedRamp = SpeedRamp.STANDARD,
            visualFilter = VisualFilter.ANAMORPHIC_FLARE,
            transition = SceneTransition.CROSS_DISSOLVE,
            imageDrawableRes = R.drawable.img_hero_cyberpunk,
            narrationText = "En el horizonte de Neo-Tokio, los destellos anamórficos anuncian el inicio del metraje."
        ),
        CinematicShot(
            id = "shot_2",
            title = "Aproximación Orbital Espacial",
            promptDescription = "Nave nodriza en órbita alrededor de un planeta gigante con llamarada solar y niebla volumétrica",
            durationSeconds = 3.5f,
            cameraMotion = CameraMotion.DOLLY_IN,
            speedRamp = SpeedRamp.SLO_MO_HALF,
            visualFilter = VisualFilter.VOLUMETRIC_FOG,
            transition = SceneTransition.GLITCH_ZOOM,
            imageDrawableRes = R.drawable.img_hero_fantasy,
            narrationText = "La nave rompe la atmósfera superior mientras la luz estelar baña los paneles térmicos."
        ),
        CinematicShot(
            id = "shot_3",
            title = "Barrido Aéreo & Clímax Visual",
            promptDescription = "Giro orbital de dron 360 grados sobre rascacielos con lluvia de partículas incandescentes",
            durationSeconds = 3.5f,
            cameraMotion = CameraMotion.ORBITAL_DRONE,
            speedRamp = SpeedRamp.FAST_ACTION,
            visualFilter = VisualFilter.EMBER_PARTICLES,
            transition = SceneTransition.FADE_TO_BLACK,
            imageDrawableRes = R.drawable.img_hero_cyberpunk,
            narrationText = "El montaje concluye con máxima resolución 4K y acabado de grano de película de 35 milímetros."
        )
    )

    private val _project = MutableStateFlow(
        CinematicProject(
            id = "proj_alpha",
            title = "Crónicas de Silicio & Luz",
            genre = "Cyberpunk 2099",
            shots = initialShots,
            aspectRatio = AspectRatio.CINEMASCOPE,
            resolution = ExportResolution.UHD_4K,
            colorGrade = ColorGrade.BLADE_RUNNER,
            avatarConfig = AvatarTrackConfig(
                isEnabled = true,
                selectedAvatarId = "sophia",
                placement = AvatarPlacement.BOTTOM_RIGHT,
                scaleFraction = 0.35f,
                scriptText = "Bienvenidos a la producción. Todos los efectos y avatares con movimiento son 100% gratis."
            )
        )
    )
    val project: StateFlow<CinematicProject> = _project.asStateFlow()

    private val _currentPlaybackSeconds = MutableStateFlow(0f)
    val currentPlaybackSeconds: StateFlow<Float> = _currentPlaybackSeconds.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _selectedShotIndex = MutableStateFlow(0)
    val selectedShotIndex: StateFlow<Int> = _selectedShotIndex.asStateFlow()

    private val _lastVoiceCommand = MutableStateFlow<VoiceCommandResult?>(null)
    val lastVoiceCommand: StateFlow<VoiceCommandResult?> = _lastVoiceCommand.asStateFlow()

    private var playbackJob: Job? = null

    init {
        startPlaybackLoop()
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (isActive) {
                delay(50)
                if (_isPlaying.value) {
                    val total = _project.value.totalDurationSeconds.coerceAtLeast(1f)
                    var next = _currentPlaybackSeconds.value + 0.05f
                    if (next >= total) {
                        next = 0f
                    }
                    _currentPlaybackSeconds.value = next
                }
            }
        }
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
    }

    fun seekSeconds(seconds: Float) {
        val total = _project.value.totalDurationSeconds.coerceAtLeast(1f)
        _currentPlaybackSeconds.value = seconds.coerceIn(0f, total)
    }

    fun selectShot(index: Int) {
        if (index in _project.value.shots.indices) {
            _selectedShotIndex.value = index
            // Seek playhead to beginning of this shot
            val shotStart = _project.value.shots.take(index).sumOf { it.durationSeconds.toDouble() }.toFloat()
            _currentPlaybackSeconds.value = shotStart
        }
    }

    fun updateProject(updated: CinematicProject) {
        _project.value = updated
        // Bound selected index if shots list changed
        if (_selectedShotIndex.value !in updated.shots.indices) {
            _selectedShotIndex.value = (updated.shots.size - 1).coerceAtLeast(0)
        }
    }

    fun updateAvatarConfig(avatarConfig: AvatarTrackConfig) {
        _project.value = _project.value.copy(avatarConfig = avatarConfig)
    }

    fun processVoiceCommand(text: String) {
        val (updatedProj, result) = voiceEngine.parseCommand(
            spokenText = text,
            activeShotIndex = _selectedShotIndex.value,
            currentProject = _project.value
        )
        _project.value = updatedProj
        _lastVoiceCommand.value = result
    }

    fun generateNewSequence(prompt: String, genre: String, shotsCount: Int) {
        val count = shotsCount.coerceIn(2, 5)
        val generatedShots = mutableListOf<CinematicShot>()

        val motions = listOf(
            CameraMotion.PAN_RIGHT,
            CameraMotion.DOLLY_IN,
            CameraMotion.ORBITAL_DRONE,
            CameraMotion.RACK_FOCUS,
            CameraMotion.FPV_DIVE
        )

        val filters = listOf(
            VisualFilter.ANAMORPHIC_FLARE,
            VisualFilter.VOLUMETRIC_FOG,
            VisualFilter.EMBER_PARTICLES,
            VisualFilter.RAIN_MIST,
            VisualFilter.FILM_GRAIN
        )

        for (i in 0 until count) {
            val imageRes = if (i % 2 == 0) R.drawable.img_hero_cyberpunk else R.drawable.img_hero_fantasy
            generatedShots.add(
                CinematicShot(
                    id = "gen_shot_${System.currentTimeMillis()}_$i",
                    title = "Plano ${i + 1}: ${genre.take(12)}",
                    promptDescription = "$prompt • Toma ${i + 1} con estilo $genre y lente anamórfico",
                    durationSeconds = if (i == 0) 4.0f else 3.5f,
                    cameraMotion = motions[i % motions.size],
                    speedRamp = if (i == 1) SpeedRamp.SLO_MO_HALF else SpeedRamp.STANDARD,
                    visualFilter = filters[i % filters.size],
                    transition = if (i == count - 1) SceneTransition.FADE_TO_BLACK else SceneTransition.CROSS_DISSOLVE,
                    imageDrawableRes = imageRes,
                    narrationText = "Secuencia generada con IA para $genre. Ajustada con óptica y atmósfera cinematográfica."
                )
            )
        }

        val luts = when {
            genre.contains("Cyber", true) -> ColorGrade.BLADE_RUNNER
            genre.contains("Fantasy", true) -> ColorGrade.GOLDEN_HOUR
            genre.contains("Noir", true) -> ColorGrade.NOIR_MONOCHROME
            else -> ColorGrade.TEAL_AND_ORANGE
        }

        val newProj = _project.value.copy(
            title = prompt.take(30).replaceFirstChar { it.uppercase() },
            genre = genre,
            shots = generatedShots,
            colorGrade = luts
        )

        _project.value = newProj
        _selectedShotIndex.value = 0
        _currentPlaybackSeconds.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
        voiceEngine.destroy()
        ttsController.shutdown()
    }
}
