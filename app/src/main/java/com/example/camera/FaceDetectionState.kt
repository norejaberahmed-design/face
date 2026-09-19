package com.example.camera

import android.graphics.Rect

/**
 * Real-time face detection telemetry produced by Google ML Kit Face Detection
 * Not constrained to 468 or any fixed number of points; supports arbitrary scale,
 * proximity (zoomed in / zoomed out), contours, and dynamic landmarks.
 */
data class FaceDetectionState(
    val faceDetected: Boolean = false,
    val faceCount: Int = 0,
    val faceBounds: Rect? = null,
    val normalizedBounds: NormalizedFaceRect? = null,
    val isSmiling: Boolean = false,
    val leftEyeOpen: Boolean = true,
    val rightEyeOpen: Boolean = true,
    val headEulerAngleY: Float = 0f, // Yaw (left/right rotation)
    val headEulerAngleX: Float = 0f, // Pitch (up/down rotation)
    val headEulerAngleZ: Float = 0f, // Roll (tilt)
    val landmarkPoints: List<Pair<Float, Float>> = emptyList(),
    val contourPoints: List<Pair<Float, Float>> = emptyList(),
    val relativeFaceWidthFraction: Float = 0.5f // Ratio of face width to frame width (zoom/proximity indicator)
)

data class NormalizedFaceRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)
