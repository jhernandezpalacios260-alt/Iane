package com.example.model

data class VoiceCommandResult(
    val spokenText: String,
    val recognizedIntent: String,
    val changesApplied: List<String>,
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)

sealed class VoiceAction {
    data class TrimShot(val shotIndex: Int, val newDurationSeconds: Float) : VoiceAction()
    data class ChangeSpeed(val shotIndex: Int, val speedRamp: SpeedRamp) : VoiceAction()
    data class SetFilter(val shotIndex: Int, val filter: VisualFilter) : VoiceAction()
    data class SetColorGrade(val colorGrade: ColorGrade) : VoiceAction()
    data class SetCameraMotion(val shotIndex: Int, val motion: CameraMotion) : VoiceAction()
    data class SetTransition(val shotIndex: Int, val transition: SceneTransition) : VoiceAction()
    data class ConfigureAvatar(val enabled: Boolean, val avatarId: String?, val placement: AvatarPlacement?) : VoiceAction()
    data class SetNarration(val text: String) : VoiceAction()
    data class SetResolution(val resolution: ExportResolution) : VoiceAction()
    data class SetAspectRatio(val aspectRatio: AspectRatio) : VoiceAction()
    data class AddNewScene(val title: String, val prompt: String) : VoiceAction()
    data class Unknown(val rawText: String) : VoiceAction()
}
