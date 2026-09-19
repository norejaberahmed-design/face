package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.FaceDetectionState
import com.example.domain.FaceMeshGenerator
import com.example.domain.LandmarkPoint
import com.example.domain.MeshEdge
import com.example.domain.RegionPolygon
import com.example.ui.theme.BeautyBlue
import com.example.ui.theme.BeautyCardBorder
import com.example.ui.theme.BeautyCyan
import com.example.ui.theme.BeautyDanger
import com.example.ui.theme.BeautyPrimary
import com.example.ui.theme.BeautyPurple
import com.example.ui.theme.BeautyYellow

@Composable
fun CameraOverlay(
    overlayType: String, // "points", "mesh", "exclusion", "regions", "pose", "lighting"
    landmarks: List<LandmarkPoint>,
    meshTopology: List<MeshEdge>,
    skinRegions: List<RegionPolygon>,
    poseYaw: Float = 0.8f,
    posePitch: Float = -0.6f,
    poseRoll: Float = 0.2f,
    distanceScore: Float = 0.94f,
    lightingScore: Float = 0.92f,
    sharpnessScore: Float = 0.96f,
    hasCameraPermission: Boolean = true,
    isFrontCamera: Boolean = true,
    mlKitFaceState: FaceDetectionState = FaceDetectionState(),
    onMlKitFaceStateChanged: (FaceDetectionState) -> Unit = {},
    onBrightnessEvaluated: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Scanning laser line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_laser_progress"
    )

    // Pulse animation for points
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val isFaceActuallyDetected = mlKitFaceState.faceDetected
    val isFaceCentered = mlKitFaceState.faceDetected &&
            kotlin.math.abs(mlKitFaceState.headEulerAngleY) < 18f &&
            kotlin.math.abs(mlKitFaceState.headEulerAngleX) < 18f

    // Dynamic distance calculation from actual face bounding size in frame:
    // When far away: faceWidthFraction is small (~0.15). When close: (~0.65).
    // Ideal range is 0.20 to 0.80.
    val actualDistanceFactor = if (mlKitFaceState.faceDetected) {
        val frac = mlKitFaceState.relativeFaceWidthFraction
        // If between 0.22 and 0.75, it's a great capture distance
        when {
            frac < 0.18f -> 0.40f // Too far away
            frac > 0.82f -> 0.45f // Too close
            else -> 0.95f // Optimal range
        }
    } else {
        distanceScore
    }

    val actualYaw = if (mlKitFaceState.faceDetected) mlKitFaceState.headEulerAngleY else poseYaw

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF101622))
            .border(width = 1.5.dp, color = BeautyCardBorder, shape = RoundedCornerShape(20.dp))
            .testTag("camera_view_container"),
        contentAlignment = Alignment.Center
    ) {
        // Layer 1: Live Hardware Camera Stream (CameraX) + ML Kit Face Detection
        if (hasCameraPermission) {
            RealCameraPreview(
                isFrontCamera = isFrontCamera,
                onFaceStateChanged = onMlKitFaceStateChanged,
                onBrightnessEvaluated = onBrightnessEvaluated,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Biometric face guide silhouette fallback when permission is not granted
            Box(
                modifier = Modifier
                    .size(width = 210.dp, height = 270.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFF182030).copy(alpha = 0.7f))
                    .border(
                        width = 1.dp,
                        color = BeautyPrimary.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(100.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👤",
                    fontSize = 82.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }

        // Layer 2: Custom Canvas drawing for Dynamic Biometric Guide, Unconstrained Landmarks, Dynamic Contours & Regions
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("camera_overlay_canvas")
        ) {
            val w = size.width
            val h = size.height

            // 1. Corner target brackets
            drawTargetCorners(w, h)

            // 2. Dynamic Adaptive Biometric Guide (Adapts to face position and zoom)
            drawAdaptiveBiometricGuide(
                w = w,
                h = h,
                faceState = mlKitFaceState,
                faceDetected = isFaceActuallyDetected,
                poseOk = isFaceCentered,
                pulseAlpha = pulseAlpha
            )

            // 3. Dynamic Real-time Overlay Layers (Open & Not constrained to static 468 points)
            when (overlayType) {
                "points" -> {
                    val pointRadius = 3.dp.toPx()

                    // If live face is detected by ML Kit, render actual dynamic face contours and landmarks
                    if (mlKitFaceState.faceDetected && (mlKitFaceState.contourPoints.isNotEmpty() || mlKitFaceState.landmarkPoints.isNotEmpty())) {
                        // Render all unconstrained contour points in real time
                        mlKitFaceState.contourPoints.forEach { pt ->
                            val px = pt.first * w
                            val py = pt.second * h
                            drawCircle(
                                color = BeautyPrimary.copy(alpha = pulseAlpha),
                                radius = pointRadius,
                                center = Offset(px, py)
                            )
                        }

                        // Render key anatomical anchor points with target rings
                        mlKitFaceState.landmarkPoints.forEach { pt ->
                            val px = pt.first * w
                            val py = pt.second * h
                            drawCircle(
                                color = BeautyCyan,
                                radius = 5.dp.toPx(),
                                center = Offset(px, py),
                                style = Stroke(width = 1.8.dp.toPx())
                            )
                        }
                    } else {
                        // Adaptive normalized points
                        landmarks.forEach { pt ->
                            val px = pt.x * w
                            val py = pt.y * h
                            drawCircle(
                                color = BeautyPrimary.copy(alpha = pulseAlpha),
                                radius = 2.4.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                    }
                }
                "mesh" -> {
                    // 3D Topology Wireframe Mesh
                    if (mlKitFaceState.faceDetected && mlKitFaceState.contourPoints.size >= 10) {
                        val pts = mlKitFaceState.contourPoints
                        for (i in 0 until pts.size - 1) {
                            val p1 = pts[i]
                            val p2 = pts[i + 1]
                            drawLine(
                                color = BeautyBlue.copy(alpha = 0.70f),
                                start = Offset(p1.first * w, p1.second * h),
                                end = Offset(p2.first * w, p2.second * h),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    } else {
                        meshTopology.forEach { edge ->
                            val p1 = landmarks.getOrNull(edge.start)
                            val p2 = landmarks.getOrNull(edge.end)
                            if (p1 != null && p2 != null) {
                                drawLine(
                                    color = BeautyBlue.copy(alpha = 0.55f),
                                    start = Offset(p1.x * w, p1.y * h),
                                    end = Offset(p2.x * w, p2.y * h),
                                    strokeWidth = 1.2.dp.toPx()
                                )
                            }
                        }
                    }
                }
                "exclusion" -> {
                    // Exclusion Masks (Eyes, Lips)
                    val exclusionPolygons = FaceMeshGenerator.getExclusionZones()
                    exclusionPolygons.forEach { pts ->
                        val path = Path()
                        if (pts.isNotEmpty()) {
                            path.moveTo(pts[0].x * w, pts[0].y * h)
                            for (i in 1 until pts.size) {
                                path.lineTo(pts[i].x * w, pts[i].y * h)
                            }
                            path.close()
                            drawPath(
                                path = path,
                                color = BeautyDanger.copy(alpha = 0.35f),
                                style = Fill
                            )
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                            drawPath(
                                path = path,
                                color = BeautyDanger,
                                style = Stroke(width = 2.dp.toPx(), pathEffect = dashEffect)
                            )
                        }
                    }
                }
                "regions" -> {
                    // 5 Skin Analysis Zones with distinct color fills
                    skinRegions.forEach { region ->
                        val color = when (region.id) {
                            "forehead" -> BeautyYellow
                            "left_cheek" -> BeautyBlue
                            "right_cheek" -> BeautyPurple
                            "t_zone" -> BeautyCyan
                            "chin" -> BeautyPrimary
                            else -> BeautyPrimary
                        }
                        val path = Path()
                        val pts = region.points
                        if (pts.isNotEmpty()) {
                            path.moveTo(pts[0].x * w, pts[0].y * h)
                            for (i in 1 until pts.size) {
                                path.lineTo(pts[i].x * w, pts[i].y * h)
                            }
                            path.close()
                            drawPath(
                                path = path,
                                color = color.copy(alpha = 0.22f),
                                style = Fill
                            )
                            drawPath(
                                path = path,
                                color = color.copy(alpha = 0.85f),
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }
                }
                "pose" -> {
                    val centerX = 0.5f * w
                    val centerY = 0.52f * h

                    // Horizontal eye level reference line
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(0.15f * w, 0.44f * h),
                        end = Offset(0.85f * w, 0.44f * h),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Vertical facial symmetry axis
                    drawLine(
                        color = BeautyCyan.copy(alpha = 0.7f),
                        start = Offset(centerX, 0.16f * h),
                        end = Offset(centerX, 0.86f * h),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Real-time Pitch & Yaw compass circle
                    val compassCenter = Offset(centerX, centerY)
                    drawCircle(
                        color = BeautyPrimary.copy(alpha = 0.3f),
                        radius = 48.dp.toPx(),
                        center = compassCenter,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    val yawAngleOffset = actualYaw * 2.2f
                    val pitchAngleOffset = (if (mlKitFaceState.faceDetected) mlKitFaceState.headEulerAngleX else posePitch) * 2.2f
                    drawCircle(
                        color = if (kotlin.math.abs(actualYaw) < 18f) BeautyPrimary else BeautyDanger,
                        radius = 7.dp.toPx(),
                        center = Offset(centerX + yawAngleOffset, centerY + pitchAngleOffset)
                    )
                }
                "lighting" -> {
                    val scanY = scanLineProgress * h
                    val laserGradient = Brush.verticalGradient(
                        colors = listOf(
                            BeautyPrimary.copy(alpha = 0f),
                            BeautyPrimary.copy(alpha = 0.85f),
                            BeautyPrimary.copy(alpha = 0f)
                        ),
                        startY = scanY - 24.dp.toPx(),
                        endY = scanY + 24.dp.toPx()
                    )
                    drawRect(
                        brush = laserGradient,
                        topLeft = Offset(0.12f * w, scanY - 20.dp.toPx()),
                        size = Size(0.76f * w, 40.dp.toPx())
                    )
                    drawLine(
                        color = BeautyCyan,
                        start = Offset(0.10f * w, scanY),
                        end = Offset(0.90f * w, scanY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }

        // Real-time Metrics Panel HUD
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xCC000000))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .testTag("metrics_panel")
        ) {
            val yawPass = kotlin.math.abs(actualYaw) < 18f
            val distancePass = actualDistanceFactor >= 0.70f
            val lightPass = lightingScore > 0.6f
            val sharpPass = sharpnessScore > 0.7f

            MetricBarRow(label = "الإضاءة", value = lightingScore, pass = lightPass)
            Spacer(modifier = Modifier.height(3.dp))
            MetricBarRow(label = "الوضوح", value = sharpnessScore, pass = sharpPass)
            Spacer(modifier = Modifier.height(3.dp))
            MetricBarRow(label = "المسافة والتقريب", value = actualDistanceFactor, pass = distancePass)
            Spacer(modifier = Modifier.height(3.dp))
            MetricBarRow(
                label = "الوضعية (Yaw)",
                value = (1f - (kotlin.math.abs(actualYaw) / 30f)).coerceIn(0f, 1f),
                pass = yawPass
            )
        }

        // Guidance Banner
        val dynamicGuidanceMessage = when {
            !mlKitFaceState.faceDetected && landmarks.isEmpty() -> "وجّه وجهك أمام الكاميرا"
            mlKitFaceState.faceCount > 1 -> "يوجد أكثر من وجه في الكادر ⚠️"
            mlKitFaceState.faceDetected && mlKitFaceState.relativeFaceWidthFraction < 0.18f -> "اقترب قليلاً من الكاميرا 🔍"
            mlKitFaceState.faceDetected && mlKitFaceState.relativeFaceWidthFraction > 0.82f -> "ابتعد قليلاً عن الكاميرا 📏"
            kotlin.math.abs(mlKitFaceState.headEulerAngleY) > 18 -> "أدر رأسك لليسار أو لليمين قليلاً"
            kotlin.math.abs(mlKitFaceState.headEulerAngleX) > 18 -> "ارفع أو اخفض رأسك قليلاً"
            !mlKitFaceState.leftEyeOpen || !mlKitFaceState.rightEyeOpen -> "يرجى فتح عينيك"
            else -> "ثابت... جاهز للتحليل ✅"
        }

        val dynamicGuidanceColor = when {
            !mlKitFaceState.faceDetected && landmarks.isEmpty() -> Color.White
            mlKitFaceState.faceCount > 1 -> BeautyDanger
            mlKitFaceState.faceDetected && (mlKitFaceState.relativeFaceWidthFraction < 0.18f || mlKitFaceState.relativeFaceWidthFraction > 0.82f) -> BeautyYellow
            kotlin.math.abs(mlKitFaceState.headEulerAngleY) > 18 || kotlin.math.abs(mlKitFaceState.headEulerAngleX) > 18 -> BeautyYellow
            !mlKitFaceState.leftEyeOpen || !mlKitFaceState.rightEyeOpen -> BeautyYellow
            else -> BeautyPrimary
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xEE000000))
                .border(1.dp, dynamicGuidanceColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 7.dp)
                .testTag("face_status_bar"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dynamicGuidanceColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dynamicGuidanceMessage,
                color = dynamicGuidanceColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MetricBarRow(label: String, value: Float, pass: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.width(86.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0x33FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = value.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (pass) BeautyPrimary else BeautyDanger)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${(value * 100).toInt()}%",
            color = if (pass) BeautyPrimary else BeautyDanger,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp)
        )
    }
}

private fun DrawScope.drawTargetCorners(w: Float, h: Float) {
    val strokeWidth = 2.dp.toPx()
    val armLen = 22.dp.toPx()
    val pad = 16.dp.toPx()
    val cornerColor = BeautyPrimary.copy(alpha = 0.75f)

    // Top-Left
    drawLine(cornerColor, Offset(pad, pad), Offset(pad + armLen, pad), strokeWidth)
    drawLine(cornerColor, Offset(pad, pad), Offset(pad, pad + armLen), strokeWidth)

    // Top-Right
    drawLine(cornerColor, Offset(w - pad, pad), Offset(w - pad - armLen, pad), strokeWidth)
    drawLine(cornerColor, Offset(w - pad, pad), Offset(w - pad, pad + armLen), strokeWidth)

    // Bottom-Left
    drawLine(cornerColor, Offset(pad, h - pad), Offset(pad + armLen, h - pad), strokeWidth)
    drawLine(cornerColor, Offset(pad, h - pad), Offset(pad, h - pad - armLen), strokeWidth)

    // Bottom-Right
    drawLine(cornerColor, Offset(w - pad, h - pad), Offset(w - pad - armLen, h - pad), strokeWidth)
    drawLine(cornerColor, Offset(w - pad, h - pad), Offset(w - pad, h - pad - armLen), strokeWidth)
}

/**
 * Draws an Adaptive Biometric Guide that automatically tracks the face wherever it is:
 * - When face is detected: bounding box and oval frame follow the real face scale and proximity
 * - When face is distant / near: expands or shrinks naturally
 * - When no face: displays a comfortable general center oval guide
 */
private fun DrawScope.drawAdaptiveBiometricGuide(
    w: Float,
    h: Float,
    faceState: FaceDetectionState,
    faceDetected: Boolean,
    poseOk: Boolean,
    pulseAlpha: Float
) {
    val norm = faceState.normalizedBounds
    val (left, top, ovalWidth, ovalHeight) = if (faceDetected && norm != null) {
        val fW = (norm.right - norm.left) * w
        val fH = (norm.bottom - norm.top) * h
        val fLeft = norm.left * w
        val fTop = norm.top * h
        // Add pleasant biometric padding around the face
        val padX = fW * 0.15f
        val padY = fH * 0.18f
        listOf(
            (fLeft - padX).coerceAtLeast(0f),
            (fTop - padY).coerceAtLeast(0f),
            (fW + padX * 2).coerceAtMost(w),
            (fH + padY * 2).coerceAtMost(h)
        )
    } else {
        val oW = w * 0.72f
        val oH = h * 0.65f
        listOf((w - oW) / 2f, (h - oH) / 2f - 20f, oW, oH)
    }

    val strokeColor = when {
        poseOk -> BeautyPrimary
        faceDetected -> BeautyYellow
        else -> Color.White.copy(alpha = 0.70f)
    }

    val cornerLen = 22.dp.toPx()
    val frameStroke = 2.dp.toPx()
    val bracketColor = strokeColor.copy(alpha = pulseAlpha)

    // Dynamic Corner Brackets
    drawLine(bracketColor, Offset(left, top), Offset(left + cornerLen, top), frameStroke)
    drawLine(bracketColor, Offset(left, top), Offset(left, top + cornerLen), frameStroke)

    drawLine(bracketColor, Offset(left + ovalWidth, top), Offset(left + ovalWidth - cornerLen, top), frameStroke)
    drawLine(bracketColor, Offset(left + ovalWidth, top), Offset(left + ovalWidth, top + cornerLen), frameStroke)

    drawLine(bracketColor, Offset(left, top + ovalHeight), Offset(left + cornerLen, top + ovalHeight), frameStroke)
    drawLine(bracketColor, Offset(left, top + ovalHeight), Offset(left, top + ovalHeight - cornerLen), frameStroke)

    drawLine(bracketColor, Offset(left + ovalWidth, top + ovalHeight), Offset(left + ovalWidth - cornerLen, top + ovalHeight), frameStroke)
    drawLine(bracketColor, Offset(left + ovalWidth, top + ovalHeight), Offset(left + ovalWidth, top + ovalHeight - cornerLen), frameStroke)

    // Biometric Oval Guide (Adjusts smoothly to distance / zoom)
    drawOval(
        color = strokeColor,
        topLeft = Offset(left, top),
        size = Size(ovalWidth, ovalHeight),
        style = Stroke(width = 3.dp.toPx())
    )

    // Subtle inner glowing pulse oval
    drawOval(
        color = strokeColor.copy(alpha = 0.20f * pulseAlpha),
        topLeft = Offset(left - 3.dp.toPx(), top - 3.dp.toPx()),
        size = Size(ovalWidth + 6.dp.toPx(), ovalHeight + 6.dp.toPx()),
        style = Stroke(width = 1.5.dp.toPx())
    )
}
