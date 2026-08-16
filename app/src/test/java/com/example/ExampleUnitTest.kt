package com.example

import com.example.model.ResolutionMode
import com.example.model.ShutterSpeedOption
import com.example.model.ZoomCapabilities
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testZoomOpticalAndDigitalDecomposition() {
    val caps = ZoomCapabilities(
      currentZoom = 4.5f,
      minZoom = 0.5f,
      maxOpticalZoom = 3.0f,
      maxDigitalZoom = 30.0f
    )

    // With 4.5x total zoom and 3.0x max optical:
    // Optical component should be capped at 3.0x
    assertEquals(3.0f, caps.opticalZoomComponent, 0.001f)
    // Digital component should be 4.5 / 3.0 = 1.5x
    assertEquals(1.5f, caps.digitalZoomComponent, 0.001f)
  }

  @Test
  fun testZoomPurelyOptical() {
    val caps = ZoomCapabilities(
      currentZoom = 2.0f,
      minZoom = 0.5f,
      maxOpticalZoom = 3.0f,
      maxDigitalZoom = 30.0f
    )

    assertEquals(2.0f, caps.opticalZoomComponent, 0.001f)
    assertEquals(1.0f, caps.digitalZoomComponent, 0.001f)
  }

  @Test
  fun testResolutionModes() {
    assertEquals(4032, ResolutionMode.MP_12.targetWidth)
    assertEquals(3024, ResolutionMode.MP_12.targetHeight)

    assertEquals(8160, ResolutionMode.MP_50.targetWidth)
    assertEquals(6120, ResolutionMode.MP_50.targetHeight)
  }

  @Test
  fun testShutterSpeedsExist() {
    assertTrue(ShutterSpeedOption.ALL.isNotEmpty())
    assertEquals("Auto", ShutterSpeedOption.ALL.first().display)
  }
}
