package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.BeautyViewModel
import com.example.ui.screens.CameraSimulatorScreen
import com.example.ui.screens.ConsentScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.Screen1_Start
import com.example.ui.screens.Screen8_Ready
import com.example.ui.theme.BeautyBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BeautyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BeautyBackground
                ) {
                    BeautyApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun BeautyApp(viewModel: BeautyViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle system back navigation gracefully
    BackHandler(enabled = state.currentScreen != AppScreen.START && state.currentScreen != AppScreen.CONSENT) {
        viewModel.previousScreen()
    }

    when (state.currentScreen) {
        AppScreen.CONSENT -> {
            ConsentScreen(
                onAgree = { viewModel.onConsentAgreed() }
            )
        }

        AppScreen.START -> {
            Screen1_Start(
                savedSessions = state.savedSessions,
                onStartClick = { viewModel.startNewSession() },
                onHistoryClick = { viewModel.goToScreen(AppScreen.HISTORY) },
                onConsentClick = { viewModel.showConsentScreen() }
            )
        }

        AppScreen.DETECT -> {
            CameraSimulatorScreen(
                currentScreen = AppScreen.DETECT,
                instruction = "جاري الكشف عن الوجه...",
                overlayType = "points",
                landmarks = state.landmarks,
                meshTopology = state.meshTopology,
                skinRegions = state.skinRegions,
                poseYaw = state.poseYaw,
                posePitch = state.posePitch,
                poseRoll = state.poseRoll,
                distanceScore = state.distanceScore,
                lightingScore = state.lightingScore,
                sharpnessScore = state.sharpnessScore,
                onNextClick = { viewModel.nextScreen() },
                onPrevClick = { viewModel.previousScreen() },
                onSkipToReady = { viewModel.goToScreen(AppScreen.READY) },
                onStepSelect = { screen -> viewModel.goToScreen(screen) },
                onHistoryClick = { viewModel.goToScreen(AppScreen.HISTORY) }
            )
        }

        AppScreen.MESH -> {
            CameraSimulatorScreen(
                currentScreen = AppScreen.MESH,
                instruction = "جاري بناء شبكة الوجه...",
                overlayType = "mesh",
                landmarks = state.landmarks,
                meshTopology = state.meshTopology,
                skinRegions = state.skinRegions,
                poseYaw = state.poseYaw,
                posePitch = state.posePitch,
                poseRoll = state.poseRoll,
                distanceScore = state.distanceScore,
                lightingScore = state.lightingScore,
                sharpnessScore = state.sharpnessScore,
                onNextClick = { viewModel.nextScreen() },
                onPrevClick = { viewModel.previousScreen() },
                onSkipToReady = { viewModel.goToScreen(AppScreen.READY) },
                onStepSelect = { screen -> viewModel.goToScreen(screen) },
                onHistoryClick = { viewModel.goToScreen(AppScreen.HISTORY) }
            )
        }

        AppScreen.EXCLUSION -> {
            CameraSimulatorScreen(
                currentScreen = AppScreen.EXCLUSION,
                instruction = "جاري تطبيق مناطق الاستبعاد...",
                overlayType = "exclusion",
                landmarks = state.landmarks,
                meshTopology = state.meshTopology,
                skinRegions = state.skinRegions,
                poseYaw = state.poseYaw,
                posePitch = state.posePitch,
                poseRoll = state.poseRoll,
                distanceScore = state.distanceScore,
                lightingScore = state.lightingScore,
                sharpnessScore = state.sharpnessScore,
                onNextClick = { viewModel.nextScreen() },
                onPrevClick = { viewModel.previousScreen() },
                onSkipToReady = { viewModel.goToScreen(AppScreen.READY) },
                onStepSelect = { screen -> viewModel.goToScreen(screen) },
                onHistoryClick = { viewModel.goToScreen(AppScreen.HISTORY) }
            )
        }

        AppScreen.REGIONS -> {
            CameraSimulatorScreen(
                currentScreen = AppScreen.REGIONS,
                instruction = "جاري تحديد مناطق البشرة...",
                overlayType = "regions",
                landmarks = state.landmarks,
                meshTopology = state.meshTopology,
                skinRegions = state.skinRegions,
                poseYaw = state.poseYaw,
                posePitch = state.posePitch,
                poseRoll = state.poseRoll,
                distanceScore = state.distanceScore,
                lightingScore = state.lightingScore,
                sharpnessScore = state.sharpnessScore,
                onNextClick = { viewModel.nextScreen() },
                onPrevClick = { viewModel.previousScreen() },
                onSkipToReady = { viewModel.goToScreen(AppScreen.READY) },
                onStepSelect = { screen -> viewModel.goToScreen(screen) },
                onHistoryClick = { viewModel.goToScreen(AppScreen.HISTORY) }
            )
        }

        AppScreen.POSE -> {
            CameraSimulatorScreen(
                currentScreen = AppScreen.POSE,
                instruction = "جاري التحقق من الوضعية والمسافة...",
                overlayType = "pose",
                landmarks = state.landmarks,
                meshTopology = state.meshTopology,
                skinRegions = state.skinRegions,
                poseYaw = state.poseYaw,
                posePitch = state.posePitch,
                poseRoll = state.poseRoll,
                distanceScore = state.distanceScore,
                lightingScore = state.lightingScore,
                sharpnessScore = state.sharpnessScore,
                onNextClick = { viewModel.nextScreen() },
                onPrevClick = { viewModel.previousScreen() },
                onSkipToReady = { viewModel.goToScreen(AppScreen.READY) },
                onStepSelect = { screen -> viewModel.goToScreen(screen) },
                onHistoryClick = { viewModel.goToScreen(AppScreen.HISTORY) }
            )
        }

        AppScreen.LIGHTING -> {
            CameraSimulatorScreen(
                currentScreen = AppScreen.LIGHTING,
                instruction = "جاري التحقق من الإضاءة والوضوح...",
                overlayType = "lighting",
                landmarks = state.landmarks,
                meshTopology = state.meshTopology,
                skinRegions = state.skinRegions,
                poseYaw = state.poseYaw,
                posePitch = state.posePitch,
                poseRoll = state.poseRoll,
                distanceScore = state.distanceScore,
                lightingScore = state.lightingScore,
                sharpnessScore = state.sharpnessScore,
                onNextClick = { viewModel.nextScreen() },
                onPrevClick = { viewModel.previousScreen() },
                onSkipToReady = { viewModel.goToScreen(AppScreen.READY) },
                onStepSelect = { screen -> viewModel.goToScreen(screen) },
                onHistoryClick = { viewModel.goToScreen(AppScreen.HISTORY) }
            )
        }

        AppScreen.READY, AppScreen.ANALYZING -> {
            Screen8_Ready(
                checklist = state.checklist,
                isAnalyzing = state.isAnalyzing,
                analysisProgress = state.analysisProgress,
                analysisStatusMessage = state.analysisStatusMessage,
                onStartAnalysis = { viewModel.startFullAnalysis() },
                onBackClick = { viewModel.previousScreen() },
                onHistoryClick = { viewModel.goToScreen(AppScreen.HISTORY) }
            )
        }

        AppScreen.REPORT -> {
            val report = state.finalReport
            if (report != null) {
                ReportScreen(
                    report = report,
                    onBackToHome = { viewModel.goToScreen(AppScreen.START) },
                    onRetakeScan = { viewModel.startNewSession() },
                    onHistoryClick = { viewModel.goToScreen(AppScreen.HISTORY) }
                )
            } else {
                viewModel.goToScreen(AppScreen.START)
            }
        }

        AppScreen.HISTORY -> {
            HistoryScreen(
                sessions = state.savedSessions,
                onSessionClick = { session -> viewModel.openReportForSession(session) },
                onDeleteSession = { sessionId -> viewModel.deleteSession(sessionId) },
                onBackClick = { viewModel.goToScreen(AppScreen.START) },
                onStartNewClick = { viewModel.startNewSession() }
            )
        }
    }
}
