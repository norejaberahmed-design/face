package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.camera.FaceDetectionState
import com.example.domain.LandmarkPoint
import com.example.domain.MeshEdge
import com.example.domain.RegionPolygon
import com.example.ui.AppScreen
import com.example.ui.components.BeautyBottomBar
import com.example.ui.components.BeautyHeader
import com.example.ui.components.CameraOverlay
import com.example.ui.components.StepProgressIndicator
import com.example.ui.theme.BeautyBackground
import com.example.ui.theme.BeautyBlue
import com.example.ui.theme.BeautyCardBorder
import com.example.ui.theme.BeautyCyan
import com.example.ui.theme.BeautyDanger
import com.example.ui.theme.BeautyPrimary
import com.example.ui.theme.BeautyPurple
import com.example.ui.theme.BeautySurface
import com.example.ui.theme.BeautyText
import com.example.ui.theme.BeautyTextMuted
import com.example.ui.theme.BeautyYellow

@Composable
fun CameraSimulatorScreen(
    currentScreen: AppScreen,
    instruction: String,
    overlayType: String,
    landmarks: List<LandmarkPoint>,
    meshTopology: List<MeshEdge>,
    skinRegions: List<RegionPolygon>,
    poseYaw: Float,
    posePitch: Float,
    poseRoll: Float,
    distanceScore: Float,
    lightingScore: Float,
    sharpnessScore: Float,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onSkipToReady: () -> Unit,
    onStepSelect: (AppScreen) -> Unit,
    onHistoryClick: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var isFrontCamera by remember { mutableStateOf(true) }
    var mlKitFaceState by remember { mutableStateOf(FaceDetectionState()) }
    var dynamicLightingScore by remember { mutableStateOf(lightingScore) }

    val allConditionsPassed = remember(poseYaw, distanceScore, dynamicLightingScore, sharpnessScore, mlKitFaceState) {
        val poseOk = if (mlKitFaceState.faceDetected) {
            kotlin.math.abs(mlKitFaceState.headEulerAngleY) < 20f &&
                    kotlin.math.abs(mlKitFaceState.headEulerAngleX) < 20f &&
                    mlKitFaceState.faceCount == 1
        } else {
            true // Allow testing or simulation if face is not actively tracked
        }
        val distanceOk = if (mlKitFaceState.faceDetected) {
            mlKitFaceState.relativeFaceWidthFraction in 0.15f..0.85f
        } else {
            true
        }
        poseOk && distanceOk && dynamicLightingScore > 0.35f && sharpnessScore > 0.5f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeautyBackground)
            .testTag("camera_simulator_screen")
    ) {
        // Top Header with flip camera toggle
        BeautyHeader(
            title = "Beauty Intelligence",
            showBackButton = true,
            onBackClick = onPrevClick,
            onHistoryClick = onHistoryClick,
            showCameraFlip = true,
            onCameraFlipClick = { isFrontCamera = !isFrontCamera }
        )

        // Step tabs bar
        StepProgressIndicator(
            currentScreen = currentScreen,
            onStepClick = onStepSelect
        )

        // Center Viewport
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasCameraPermission) {
                // Permission prompt card if denied
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BeautySurface)
                        .border(1.dp, BeautyCardBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "يرجى منح إذن الكاميرا لرؤية وجهك مباشرة في التطبيق",
                        color = BeautyText,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = BeautyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("تفعيل الكاميرا", color = BeautyBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Live Viewfinder Camera Overlay
            CameraOverlay(
                overlayType = overlayType,
                landmarks = landmarks,
                meshTopology = meshTopology,
                skinRegions = skinRegions,
                poseYaw = poseYaw,
                posePitch = posePitch,
                poseRoll = poseRoll,
                distanceScore = distanceScore,
                lightingScore = lightingScore,
                sharpnessScore = sharpnessScore,
                hasCameraPermission = hasCameraPermission,
                isFrontCamera = isFrontCamera,
                mlKitFaceState = mlKitFaceState,
                onMlKitFaceStateChanged = { mlKitFaceState = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Step telemetry badge / description
            when (currentScreen) {
                AppScreen.DETECT -> {
                    val pointsCountText = if (mlKitFaceState.faceDetected && mlKitFaceState.contourPoints.isNotEmpty()) {
                        "رصد ملامح حية ديناميكية (${mlKitFaceState.contourPoints.size} نقطة محيطية)"
                    } else {
                        "كشف وتتبع معالم الوجه بدقة وتدرج مفتوح"
                    }
                    LiveBadge(
                        icon = "📍",
                        text = pointsCountText,
                        badgeColor = BeautyPrimary
                    )
                }
                AppScreen.MESH -> {
                    LiveBadge(
                        icon = "🕸️",
                        text = "طوبولوجيا الشبكة: 342 ارتباطاً هندسياً ثلاثي الأبعاد",
                        badgeColor = BeautyBlue
                    )
                }
                AppScreen.EXCLUSION -> {
                    LiveBadge(
                        icon = "🚫",
                        text = "عزل دقيق لمنطقة العينين والرموش والشفتين",
                        badgeColor = BeautyDanger
                    )
                }
                AppScreen.REGIONS -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RegionMiniPill("الجبهة", BeautyYellow)
                        RegionMiniPill("الخد الأيسر", BeautyBlue)
                        RegionMiniPill("الخد الأيمن", BeautyPurple)
                        RegionMiniPill("منطقة T", BeautyCyan)
                        RegionMiniPill("الذقن", BeautyPrimary)
                    }
                }
                AppScreen.POSE -> {
                    LiveBadge(
                        icon = "📐",
                        text = "المسافة: 35 سم (${(distanceScore * 100).toInt()}%) • زاوية الميل: متمركز",
                        badgeColor = BeautyPrimary
                    )
                }
                AppScreen.LIGHTING -> {
                    LiveBadge(
                        icon = "☀️",
                        text = "الإضاءة: ${(lightingScore * 100).toInt()}% • الوضوح: ${(sharpnessScore * 100).toInt()}% ممتاز",
                        badgeColor = BeautyYellow
                    )
                }
                else -> Unit
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Instruction Text matching React Native specification:
            Text(
                text = instruction,
                color = BeautyText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Navigation Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPrevClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BeautyTextMuted),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BeautyCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("السابق", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onNextClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BeautyPrimary,
                        contentColor = BeautyBackground
                    ),
                    modifier = Modifier.weight(1.4f)
                ) {
                    Text("التالي", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Bottom Bar with Direct Shutter Capture Action matching LiveCameraScreen.tsx
        BeautyBottomBar(
            allConditionsPassed = allConditionsPassed,
            onGalleryClick = onHistoryClick,
            onCaptureClick = onSkipToReady,
            onMenuClick = onSkipToReady
        )
    }
}


@Composable
private fun LiveBadge(icon: String, text: String, badgeColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BeautySurface)
            .border(width = 1.dp, color = badgeColor.copy(alpha = 0.4f), shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = BeautyText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun RegionMiniPill(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BeautySurface)
            .border(width = 1.dp, color = color.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(name, color = BeautyText, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}
