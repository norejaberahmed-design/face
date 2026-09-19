package com.example.ui.components

import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.camera.BatteryOptimizer
import com.example.camera.FaceDetectionState
import com.example.camera.LightingEnhancer
import com.example.camera.NormalizedFaceRect
import com.example.camera.ScreenLightBoost
import com.example.camera.computeAverageBrightness
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

@Composable
fun RealCameraPreview(
    isFrontCamera: Boolean = true,
    onFaceStateChanged: (FaceDetectionState) -> Unit = {},
    onBrightnessEvaluated: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraErrorMessage by remember { mutableStateOf<String?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val batteryOptimizer = remember { BatteryOptimizer() }
    val lightingEnhancer = remember { LightingEnhancer() }

    // Google ML Kit Face Detector: unconstrained landmarks, contours, and proximity tracking
    val faceDetector: FaceDetector = remember {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setMinFaceSize(0.10f) // Can detect face even when far away
            .enableTracking()
            .build()
        FaceDetection.getClient(options)
    }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(isFrontCamera, lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val mainExecutor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preferredLens = if (isFrontCamera) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }

                val hasPreferred = cameraProvider.hasCamera(
                    CameraSelector.Builder().requireLensFacing(preferredLens).build()
                )

                val cameraSelector = if (hasPreferred) {
                    CameraSelector.Builder().requireLensFacing(preferredLens).build()
                } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else if (cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    null
                }

                if (cameraSelector != null) {
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    // ImageAnalysis stream with Google ML Kit Face Detection
                    @OptIn(ExperimentalGetImage::class)
                    val imageAnalysis = BatteryOptimizer.buildOptimizedImageAnalysis()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor) { imageProxy: ImageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null) {
                                    // 1. Calculate lighting / average brightness
                                    val avgBrightness = computeAverageBrightness(imageProxy)
                                    onBrightnessEvaluated(avgBrightness)
                                    lightingEnhancer.evaluateAndAdjust(avgBrightness, isFrontCamera)

                                    // 2. Battery & thermal adaptive frame skipping
                                    batteryOptimizer.updateStrategy(context)
                                    if (!batteryOptimizer.shouldProcess()) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    val rotation = imageProxy.imageInfo.rotationDegrees
                                    val image = InputImage.fromMediaImage(mediaImage, rotation)
                                    // Effective image dimensions after taking rotation into account
                                    val isRotated = rotation == 90 || rotation == 270
                                    val imgW = if (isRotated) imageProxy.height.toFloat() else imageProxy.width.toFloat()
                                    val imgH = if (isRotated) imageProxy.width.toFloat() else imageProxy.height.toFloat()

                                    faceDetector.process(image)
                                        .addOnSuccessListener { faces ->
                                            if (faces.isEmpty()) {
                                                onFaceStateChanged(FaceDetectionState(faceDetected = false))
                                            } else {
                                                val face = faces[0]
                                                val bounds = face.boundingBox

                                                // Compute normalized coordinates [0.0..1.0] regardless of distance or resolution
                                                val normLeft = (bounds.left.toFloat() / imgW).coerceIn(0f, 1f)
                                                val normTop = (bounds.top.toFloat() / imgH).coerceIn(0f, 1f)
                                                val normRight = (bounds.right.toFloat() / imgW).coerceIn(0f, 1f)
                                                val normBottom = (bounds.bottom.toFloat() / imgH).coerceIn(0f, 1f)

                                                // Normalized contour points from ML Kit
                                                val contours = mutableListOf<Pair<Float, Float>>()
                                                face.allContours.forEach { c ->
                                                    c.points.forEach { pt ->
                                                        val nx = (pt.x / imgW).coerceIn(0f, 1f)
                                                        val ny = (pt.y / imgH).coerceIn(0f, 1f)
                                                        // If front camera, mirror horizontally for natural preview alignment
                                                        val finalX = if (isFrontCamera) 1f - nx else nx
                                                        contours.add(Pair(finalX, ny))
                                                    }
                                                }

                                                // Normalized landmark points
                                                val landmarks = mutableListOf<Pair<Float, Float>>()
                                                face.allLandmarks.forEach { lm ->
                                                    val pt = lm.position
                                                    val nx = (pt.x / imgW).coerceIn(0f, 1f)
                                                    val ny = (pt.y / imgH).coerceIn(0f, 1f)
                                                    val finalX = if (isFrontCamera) 1f - nx else nx
                                                    landmarks.add(Pair(finalX, ny))
                                                }

                                                val faceWidthFraction = (normRight - normLeft).coerceIn(0.05f, 1f)

                                                val state = FaceDetectionState(
                                                    faceDetected = true,
                                                    faceCount = faces.size,
                                                    faceBounds = bounds,
                                                    normalizedBounds = NormalizedFaceRect(
                                                        left = if (isFrontCamera) 1f - normRight else normLeft,
                                                        top = normTop,
                                                        right = if (isFrontCamera) 1f - normLeft else normRight,
                                                        bottom = normBottom
                                                    ),
                                                    isSmiling = (face.smilingProbability ?: 0f) > 0.5f,
                                                    leftEyeOpen = (face.leftEyeOpenProbability ?: 1f) > 0.5f,
                                                    rightEyeOpen = (face.rightEyeOpenProbability ?: 1f) > 0.5f,
                                                    headEulerAngleY = face.headEulerAngleY,
                                                    headEulerAngleX = face.headEulerAngleX,
                                                    headEulerAngleZ = face.headEulerAngleZ,
                                                    landmarkPoints = landmarks,
                                                    contourPoints = contours,
                                                    relativeFaceWidthFraction = faceWidthFraction
                                                )
                                                onFaceStateChanged(state)
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("RealCameraPreview", "ML Kit Face Detection error: ${e.message}")
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }
                        }

                    cameraProvider.unbindAll()
                    val boundCamera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    lightingEnhancer.updateCamera(boundCamera)
                    cameraErrorMessage = null
                } else {
                    cameraErrorMessage = "لا توجد كاميرا متوفرة على هذا الجهاز"
                }
            } catch (e: Exception) {
                Log.e("RealCameraPreview", "Failed to bind camera lifecycle", e)
                cameraErrorMessage = "تعذر تشغيل الكاميرا الحية: ${e.localizedMessage}"
            }
        }, mainExecutor)

        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (ignored: Exception) {}
            cameraExecutor.shutdown()
            faceDetector.close()
        }
    }

    Box(modifier = modifier.fillMaxSize().testTag("real_camera_preview_box")) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize().testTag("camera_preview_view")
        )

        // Soft Screen Light Boost when front camera lighting is low
        ScreenLightBoost(
            enabled = isFrontCamera && lightingEnhancer.isScreenBoostActive.value,
            brightness = 1.0f
        )

        if (cameraErrorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xAA000000))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cameraErrorMessage ?: "",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}
