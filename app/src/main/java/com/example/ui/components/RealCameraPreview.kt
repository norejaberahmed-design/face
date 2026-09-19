                                                        contours.add(Pair(finalX, ny))
                                                    }
             package com.example.ui.components

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
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

    var cameraErrorMessage by remember {
        mutableStateOf<String?>(null)
    }

    /*
     * These resources live for the entire lifetime of this composable.
     * They must NOT be closed when switching front/back camera.
     */
    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    val batteryOptimizer = remember {
        BatteryOptimizer()
    }

    val lightingEnhancer = remember {
        LightingEnhancer()
    }

    val faceDetector: FaceDetector = remember {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(
                FaceDetectorOptions.PERFORMANCE_MODE_FAST
            )
            .setLandmarkMode(
                FaceDetectorOptions.LANDMARK_MODE_ALL
            )
            .setClassificationMode(
                FaceDetectorOptions.CLASSIFICATION_MODE_ALL
            )
            .setContourMode(
                FaceDetectorOptions.CONTOUR_MODE_ALL
            )
            .setMinFaceSize(0.10f)
            .enableTracking()
            .build()

        FaceDetection.getClient(options)
    }

    /*
     * PreviewView is also remembered so CameraX always binds
     * to the same Android View.
     */
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode =
                PreviewView.ImplementationMode.COMPATIBLE

            scaleType =
                PreviewView.ScaleType.FILL_CENTER
        }
    }

    /*
     * Camera lifecycle.
     *
     * This effect is recreated when:
     * - front/back camera changes
     * - lifecycle owner changes
     *
     * But executor and ML Kit detector remain alive.
     */
    DisposableEffect(
        isFrontCamera,
        lifecycleOwner
    ) {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        val mainExecutor =
            ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener(
            {

                try {

                    val cameraProvider =
                        cameraProviderFuture.get()

                    /*
                     * Select requested lens.
                     */
                    val preferredLens =
                        if (isFrontCamera) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }

                    val preferredSelector =
                        CameraSelector.Builder()
                            .requireLensFacing(preferredLens)
                            .build()

                    /*
                     * Check whether requested camera actually exists.
                     */
                    val hasPreferred =
                        cameraProvider.hasCamera(
                            preferredSelector
                        )

                    val cameraSelector =
                        when {
                            hasPreferred -> {
                                preferredSelector
                            }

                            cameraProvider.hasCamera(
                                CameraSelector.DEFAULT_BACK_CAMERA
                            ) -> {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            cameraProvider.hasCamera(
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            ) -> {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            }

                            else -> {
                                null
                            }
                        }

                    if (cameraSelector == null) {

                        cameraErrorMessage =
                            "لا توجد كاميرا متوفرة على هذا الجهاز"

                        return@addListener
                    }

                    /*
                     * CameraX Preview.
                     */
                    val preview =
                        Preview.Builder()
                            .build()
                            .also {
                                it.surfaceProvider =
                                    previewView.surfaceProvider
                            }

                    /*
                     * CameraX ImageAnalysis.
                     */
                    @OptIn(ExperimentalGetImage::class)
                    val imageAnalysis =
                        BatteryOptimizer
                            .buildOptimizedImageAnalysis()
                            .also { analysis ->

                                analysis.setAnalyzer(
                                    cameraExecutor
                                ) { imageProxy: ImageProxy ->

                                    processCameraFrame(
                                        imageProxy = imageProxy,
                                        isFrontCamera = isFrontCamera,
                                        context = context,
                                        batteryOptimizer = batteryOptimizer,
                                        lightingEnhancer = lightingEnhancer,
                                        faceDetector = faceDetector,
                                        onFaceStateChanged = onFaceStateChanged,
                                        onBrightnessEvaluated = onBrightnessEvaluated
                                    )
                                }
                            }

                    /*
                     * Remove previous CameraX bindings before
                     * binding the new camera.
                     */
                    cameraProvider.unbindAll()

                    /*
                     * Bind Preview + ImageAnalysis.
                     */
                    val boundCamera =
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )

                    lightingEnhancer.updateCamera(
                        boundCamera
                    )

                    cameraErrorMessage = null

                    Log.d(
                        "RealCameraPreview",
                        "Camera started successfully. Front=$isFrontCamera"
                    )

                } catch (e: Exception) {

                    Log.e(
                        "RealCameraPreview",
                        "Failed to bind camera lifecycle",
                        e
                    )

                    cameraErrorMessage =
                        "تعذر تشغيل الكاميرا الحية: ${e.localizedMessage}"
                }

            },
            mainExecutor
        )

        /*
         * IMPORTANT:
         *
         * When this effect is recreated, only CameraX bindings
         * are removed.
         *
         * Executor and ML Kit detector stay alive.
         */
        onDispose {

            try {

                if (cameraProviderFuture.isDone) {

                    cameraProviderFuture
                        .get()
                        .unbindAll()
                }

            } catch (ignored: Exception) {
                Log.w(
                    "RealCameraPreview",
                    "Camera cleanup warning",
                    ignored
                )
            }
        }
    }

    /*
     * Close long-lived resources ONLY when this composable
     * completely leaves the composition.
     */
    DisposableEffect(Unit) {

        onDispose {

            try {
                cameraExecutor.shutdown()
            } catch (ignored: Exception) {
            }

            try {
                faceDetector.close()
            } catch (ignored: Exception) {
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("real_camera_preview_box")
    ) {

        AndroidView(
            factory = {
                previewView
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("camera_preview_view")
        )

        /*
         * Screen light boost for front camera.
         */
        ScreenLightBoost(
            enabled =
                isFrontCamera &&
                lightingEnhancer
                    .isScreenBoostActive
                    .value,
            brightness = 1.0f
        )

        /*
         * Camera error.
         */
        if (cameraErrorMessage != null) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color(0xAA000000)
                    )
                    .padding(16.dp),
                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        cameraErrorMessage ?: "",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }
    }
}


/**
 * Processes one CameraX frame.
 *
 * This function is deliberately separated from the
 * Compose lifecycle code so ImageProxy is always closed.
 */
@OptIn(ExperimentalGetImage::class)
private fun processCameraFrame(
    imageProxy: ImageProxy,
    isFrontCamera: Boolean,
    context: android.content.Context,
    batteryOptimizer: BatteryOptimizer,
    lightingEnhancer: LightingEnhancer,
    faceDetector: FaceDetector,
    onFaceStateChanged: (FaceDetectionState) -> Unit,
    onBrightnessEvaluated: (Float) -> Unit
) {

    try {

        val mediaImage =
            imageProxy.image

        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        /*
         * 1. Lighting analysis.
         */
        val avgBrightness =
            computeAverageBrightness(
                imageProxy
            )

        onBrightnessEvaluated(
            avgBrightness
        )

        lightingEnhancer.evaluateAndAdjust(
            avgBrightness,
            isFrontCamera
        )

        /*
         * 2. Battery / thermal optimization.
         */
        batteryOptimizer.updateStrategy(
            context
        )

        if (!batteryOptimizer.shouldProcess()) {

            imageProxy.close()
            return
        }

        /*
         * 3. Rotation.
         */
        val rotation =
            imageProxy.imageInfo.rotationDegrees

        val image =
            InputImage.fromMediaImage(
                mediaImage,
                rotation
            )

        /*
         * Camera image dimensions after rotation.
         */
        val isRotated =
            rotation == 90 ||
            rotation == 270

        val imgW =
            if (isRotated) {
                imageProxy.height.toFloat()
            } else {
                imageProxy.width.toFloat()
            }

        val imgH =
            if (isRotated) {
                imageProxy.width.toFloat()
            } else {
                imageProxy.height.toFloat()
            }

        /*
         * 4. ML Kit face detection.
         */
        faceDetector
            .process(image)
            .addOnSuccessListener { faces ->

                if (faces.isEmpty()) {

                    onFaceStateChanged(
                        FaceDetectionState(
                            faceDetected = false,
                            faceCount = 0
                        )
                    )

                    return@addOnSuccessListener
                }

                /*
                 * Use the largest detected face.
                 * This prevents a small background face
                 * from becoming the primary face.
                 */
                val face =
                    faces.maxByOrNull {
                        it.boundingBox.width() *
                        it.boundingBox.height()
                    } ?: return@addOnSuccessListener

                val bounds =
                    face.boundingBox

                /*
                 * Normalized bounding box.
                 */
                val normLeft =
                    (
                        bounds.left.toFloat() /
                        imgW
                    ).coerceIn(0f, 1f)

                val normTop =
                    (
                        bounds.top.toFloat() /
                        imgH
                    ).coerceIn(0f, 1f)

                val normRight =
                    (
                        bounds.right.toFloat() /
                        imgW
                    ).coerceIn(0f, 1f)

                val normBottom =
                    (
                        bounds.bottom.toFloat() /
                        imgH
                    ).coerceIn(0f, 1f)

                /*
                 * Dynamic contour points.
                 */
                val contours =
                    mutableListOf<Pair<Float, Float>>()

                face.allContours.forEach { contour ->

                    contour.points.forEach { point ->

                        val nx =
                            (
                                point.x /
                                imgW
                            ).coerceIn(0f, 1f)

                        val ny =
                            (
                                point.y /
                                imgH
                            ).coerceIn(0f, 1f)

                        val finalX =
                            if (isFrontCamera) {
                                1f - nx
                            } else {
                                nx
                            }

                        contours.add(
                            Pair(finalX, ny)
                        )
                    }
                }

                /*
                 * Dynamic landmark points.
                 */
                val landmarks =
                    mutableListOf<Pair<Float, Float>>()

                face.allLandmarks.forEach { landmark ->

                    val point =
                        landmark.position

                    val nx =
                        (
                            point.x /
                            imgW
                        ).coerceIn(0f, 1f)

                    val ny =
                        (
                            point.y /
                            imgH
                        ).coerceIn(0f, 1f)

                    val finalX =
                        if (isFrontCamera) {
                            1f - nx
                        } else {
                            nx
                        }

                    landmarks.add(
                        Pair(finalX, ny)
                    )
                }

                /*
                 * Face size relative to camera frame.
                 */
                val faceWidthFraction =
                    (
                        normRight -
                        normLeft
                    ).coerceIn(
                        0.05f,
                        1f
                    )

                /*
                 * Build live face state.
                 */
                val state =
                    FaceDetectionState(

                        faceDetected = true,

                        faceCount = faces.size,

                        faceBounds = bounds,

                        normalizedBounds =
                            NormalizedFaceRect(
                                left =
                                    if (isFrontCamera) {
                                        1f - normRight
                                    } else {
                                        normLeft
                                    },

                                top = normTop,

                                right =
                                    if (isFrontCamera) {
                                        1f - normLeft
                                    } else {
                                        normRight
                                    },

                                bottom = normBottom
                            ),

                        isSmiling =
                            (
                                face.smilingProbability
                                    ?: 0f
                            ) > 0.5f,

                        leftEyeOpen =
                            (
                                face.leftEyeOpenProbability
                                    ?: 1f
                            ) > 0.5f,

                        rightEyeOpen =
                            (
                                face.rightEyeOpenProbability
                                    ?: 1f
                            ) > 0.5f,

                        headEulerAngleY =
                            face.headEulerAngleY,

                        headEulerAngleX =
                            face.headEulerAngleX,

                        headEulerAngleZ =
                            face.headEulerAngleZ,

                        landmarkPoints =
                            landmarks,

                        contourPoints =
                            contours,

                        relativeFaceWidthFraction =
                            faceWidthFraction
                    )

                onFaceStateChanged(
                    state
                )
            }
            .addOnFailureListener { error ->

                Log.e(
                    "RealCameraPreview",
                    "ML Kit Face Detection error",
                    error
                )
            }
            .addOnCompleteListener {

                /*
                 * CRITICAL:
                 * Every analyzed frame must eventually
                 * release its ImageProxy.
                 */
                imageProxy.close()
            }

    } catch (e: Exception) {

        Log.e(
            "RealCameraPreview",
            "Frame processing error",
            e
        )

        /*
         * Never leave ImageProxy locked.
         */
        imageProxy.close()
    }
}                                   }

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
