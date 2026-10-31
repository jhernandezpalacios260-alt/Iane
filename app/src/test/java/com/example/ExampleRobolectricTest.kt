package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.*
import com.example.voice.VoiceCommandEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CineGen", appName)
  }

  @Test
  fun `voice command parsing updates sequence duration and speed`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = VoiceCommandEngine(context)
    val testProject = CinematicProject(
        id = "test_1",
        title = "Test",
        genre = "Sci-Fi",
        shots = listOf(
            CinematicShot(
                id = "s1",
                title = "Toma 1",
                promptDescription = "Desc",
                durationSeconds = 5.0f
            )
        )
    )

    val (updated, result) = engine.parseCommand(
        spokenText = "Corta la escena a 3 segundos y pon cámara lenta",
        activeShotIndex = 0,
        currentProject = testProject
    )

    assertEquals(3.0f, updated.shots[0].durationSeconds)
    assertEquals(SpeedRamp.SLO_MO_HALF, updated.shots[0].speedRamp)
    assertTrue(result.changesApplied.isNotEmpty())
  }
}
