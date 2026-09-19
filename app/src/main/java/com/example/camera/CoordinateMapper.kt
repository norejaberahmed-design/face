package com.example.camera

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.google.mlkit.vision.face.Face

/**
 * Maps coordinates between camera analysis frame and screen view
 * accounting for rotation (90, 180, 270), mirroring for front camera, and FillCenter scaling.
 */
class CoordinateMapper(
    private val imageWidth: Int,
    private val imageHeight: Int,
    private val previewWidth: Float,
    private val previewHeight: Float,
    private val rotationDegrees: Int,
    private val isFrontCamera: Boolean
) {
    private val scaleX: Float
    private val scaleY: Float
    private val offsetX: Float
    private val offsetY: Float

    init {
        val (effectiveImageW, effectiveImageH) = if (rotationDegrees == 90 || rotationDegrees == 270) {
            imageHeight.toFloat() to imageWidth.toFloat()
        } else {
            imageWidth.toFloat() to imageHeight.toFloat()
        }

        val scaleToFit = maxOf(
            if (effectiveImageW > 0) previewWidth / effectiveImageW else 1f,
            if (effectiveImageH > 0) previewHeight / effectiveImageH else 1f
        )

        val scaledW = effectiveImageW * scaleToFit
        val scaledH = effectiveImageH * scaleToFit

        scaleX = scaleToFit
        scaleY = scaleToFit
        offsetX = (previewWidth - scaledW) / 2f
        offsetY = (previewHeight - scaledH) / 2f
    }

    /**
     * Map normalized [0..1] point to view coordinates in pixels
     */
    fun mapNormalizedToScreen(normX: Float, normY: Float): Offset {
        val px = normX * imageWidth
        val py = normY * imageHeight

        val (rotX, rotY) = rotate(px, py, rotationDegrees, imageWidth.toFloat(), imageHeight.toFloat())

        val finalX = if (isFrontCamera) {
            val effW = if (rotationDegrees == 90 || rotationDegrees == 270) imageHeight.toFloat() else imageWidth.toFloat()
            effW - rotX
        } else {
            rotX
        }

        val screenX = finalX * scaleX + offsetX
        val screenY = rotY * scaleY + offsetY
        return Offset(screenX, screenY)
    }

    private fun rotate(x: Float, y: Float, degrees: Int, w: Float, h: Float): Pair<Float, Float> {
        return when (degrees) {
            90 -> y to (w - x)
            180 -> (w - x) to (h - y)
            270 -> (h - y) to x
            else -> x to y
        }
    }
}
