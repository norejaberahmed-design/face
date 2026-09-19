package com.example.camera

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.camera.core.ImageAnalysis
import android.util.Size

/**
 * Adaptive Frame Skipper & Battery Optimization:
 * Throttles frame analysis rate dynamically based on device thermal status and processing load.
 */
class BatteryOptimizer {

    private var frameCounter = 0
    private var skipRate = 1 // 1 = every frame, 2 = every 2nd frame, etc.
    private var lastFpsUpdate = System.currentTimeMillis()
    private var processedFrames = 0
    private var currentFps = 0

    fun updateStrategy(context: Context) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val thermalStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            powerManager.currentThermalStatus
        } else {
            0
        }

        skipRate = when (thermalStatus) {
            PowerManager.THERMAL_STATUS_NONE,
            PowerManager.THERMAL_STATUS_LIGHT -> 1
            PowerManager.THERMAL_STATUS_MODERATE -> 2
            PowerManager.THERMAL_STATUS_SEVERE -> 3
            PowerManager.THERMAL_STATUS_CRITICAL,
            PowerManager.THERMAL_STATUS_EMERGENCY -> 5
            PowerManager.THERMAL_STATUS_SHUTDOWN -> 8
            else -> 1
        }

        if (isLowEndDevice()) {
            skipRate = maxOf(skipRate, 2)
        }
    }

    /**
     * @return true if frame should be analyzed by ML Kit
     */
    fun shouldProcess(): Boolean {
        frameCounter++
        if (frameCounter >= skipRate) {
            frameCounter = 0
            processedFrames++
            val now = System.currentTimeMillis()
            val elapsed = (now - lastFpsUpdate) / 1000f
            if (elapsed >= 1f) {
                currentFps = (processedFrames / elapsed).toInt()
                processedFrames = 0
                lastFpsUpdate = now
            }
            return true
        }
        return false
    }

    fun getCurrentFps(): Int = currentFps

    private fun isLowEndDevice(): Boolean {
        val cores = Runtime.getRuntime().availableProcessors()
        val maxMemoryMb = Runtime.getRuntime().maxMemory() / (1024 * 1024)
        return cores <= 4 || maxMemoryMb < 256
    }

    companion object {
        fun buildOptimizedImageAnalysis(): ImageAnalysis {
            return ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(640, 480))
                .setImageQueueDepth(1)
                .build()
        }
    }
}
