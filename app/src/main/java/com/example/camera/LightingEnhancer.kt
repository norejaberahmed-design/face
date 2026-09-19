package com.example.camera

import androidx.camera.core.Camera
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Lighting Enhancer:
 * Automatically evaluates average brightness from camera frames and adjusts hardware torch
 * or triggers a front-screen light boost.
 */
class LightingEnhancer(private var camera: Camera? = null) {

    private val _isTorchOn = mutableStateOf(false)
    val isTorchOn: State<Boolean> get() = _isTorchOn

    private val _isScreenBoostActive = mutableStateOf(false)
    val isScreenBoostActive: State<Boolean> get() = _isScreenBoostActive

    fun updateCamera(newCamera: Camera?) {
        this.camera = newCamera
    }

    /**
     * Evaluates frame brightness [0.0..1.0] and automatically boosts lighting if too dark
     */
    fun evaluateAndAdjust(averageBrightness: Float, isFrontCamera: Boolean) {
        when {
            averageBrightness < 0.30f -> {
                // Low lighting
                if (isFrontCamera) {
                    _isScreenBoostActive.value = true
                } else {
                    setTorch(true)
                }
            }
            averageBrightness > 0.55f -> {
                // Adequate lighting
                _isScreenBoostActive.value = false
                setTorch(false)
            }
        }
    }

    fun setTorch(enabled: Boolean) {
        try {
            camera?.cameraControl?.enableTorch(enabled)
            _isTorchOn.value = enabled
        } catch (_: Exception) {
            // Some cameras do not have a flash unit
        }
    }

    fun toggleTorch() {
        setTorch(!_isTorchOn.value)
    }
}

/**
 * Computes average brightness [0.0..1.0] from Y-plane in ImageProxy
 */
fun computeAverageBrightness(imageProxy: ImageProxy): Float {
    return try {
        val planes = imageProxy.planes
        if (planes.isEmpty()) return 0.5f
        val buffer = planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        var sum = 0L
        var count = 0
        // Sample every 16th pixel for high performance
        for (i in data.indices step 16) {
            sum += (data[i].toInt() and 0xFF)
            count++
        }

        if (count > 0) (sum.toFloat() / count) / 255f else 0.5f
    } catch (_: Exception) {
        0.5f
    }
}

/**
 * Screen Light Boost (Soft white glow overlay for front camera self-illumination)
 */
@Composable
fun ScreenLightBoost(
    enabled: Boolean,
    brightness: Float = 1.0f,
    modifier: Modifier = Modifier
) {
    if (!enabled) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = (brightness * 0.35f).coerceIn(0.1f, 0.6f)))
    )
}
