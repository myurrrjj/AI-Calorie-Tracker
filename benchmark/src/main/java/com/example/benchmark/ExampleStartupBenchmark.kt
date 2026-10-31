package com.myurrrjj.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TrackABiteBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun startup() = benchmarkRule.measureRepeated(
        packageName = "com.myurrrjj.trackabite",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()
    }

    @Test
    fun scrollAndSwipeJank() = benchmarkRule.measureRepeated(
        packageName = "com.myurrrjj.trackabite",
        metrics = listOf(FrameTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.WARM
    ) {
        startActivityAndWait()

        Thread.sleep(3000)

        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight

        device.swipe(
            displayWidth / 4, displayHeight / 2,
            (displayWidth * 3) / 4, displayHeight / 2,
            20
        )
        device.waitForIdle()

        device.swipe(
            (displayWidth * 3) / 4, displayHeight / 2,
            displayWidth / 4, displayHeight / 2,
            20
        )
        device.waitForIdle()
    }
}