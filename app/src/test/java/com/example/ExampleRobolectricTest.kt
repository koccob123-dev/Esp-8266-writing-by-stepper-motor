package com.example

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.test.core.app.ApplicationProvider
import com.example.engine.PathOptimizer
import com.example.engine.PlotterCoordinates
import com.example.engine.StrokeFontEngine
import com.example.engine.StrokePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    assertEquals("WriteBot Controller", appName)
  }

  @Test
  fun `stroke font engine converts text to vector strokes`() {
    val strokes = StrokeFontEngine.textToStrokes(
      text = "TEST",
      fontSizeMm = 12f,
      letterSpacingMm = 2f,
      lineSpacingMm = 4f,
      startX = 10f,
      startY = 10f
    )
    assertFalse("Strokes should not be empty for TEST", strokes.isEmpty())
    assertTrue("Each letter should have points", strokes.all { it.isNotEmpty() })
  }

  @Test
  fun `plotter coordinates canvas to machine mapping`() {
    val machineW = 100f
    val machineH = 100f
    val canvasSize = Size(500f, 500f)

    val canvasPoint = Offset(250f, 250f)
    val machinePt = PlotterCoordinates.canvasToMachine(canvasPoint, canvasSize, machineW, machineH)

    assertEquals(50f, machinePt.x, 0.5f)
    assertEquals(50f, machinePt.y, 0.5f)
  }

  @Test
  fun `path optimizer calculates distance and stats`() {
    val stroke1 = listOf(
      StrokePoint(0f, 0f),
      StrokePoint(10f, 0f),
      StrokePoint(10f, 10f)
    )
    val stroke2 = listOf(
      StrokePoint(20f, 20f),
      StrokePoint(30f, 20f)
    )
    val preflight = PathOptimizer.calculatePreflight(
      title = "Test Preflight",
      strokes = listOf(stroke1, stroke2),
      machineWidthMm = 100f,
      machineHeightMm = 100f,
      travelSpeedMmS = 50f,
      drawSpeedMmS = 30f
    )

    assertEquals(2, preflight.totalPaths)
    assertTrue("Total distance should be greater than zero", preflight.totalDistanceMm > 0f)
    assertTrue("Estimated duration should be greater than zero", preflight.estimatedDurationSeconds > 0)
  }
}

