package com.example.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BeautyPrimary

/**
 * Camera Environment Detector:
 * Detects whether running on physical device, standard emulator, or cloud test environment
 * and provides intelligent fallbacks (Test Photo simulation, image picking, or direct stream).
 */
class CameraEnvironmentDetector(private val context: Context) {

    data class Environment(
        val hasFrontCamera: Boolean,
        val hasBackCamera: Boolean,
        val isEmulator: Boolean,
        val canUseRealCamera: Boolean,
        val recommendedFallback: FallbackMode
    )

    enum class FallbackMode {
        REAL_CAMERA,
        TEST_IMAGE,
        MANUAL_SIMULATION
    }

    fun detect(): Environment {
        val isEmulator = isRunningOnEmulator()

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        val cameraIds = try {
            cameraManager?.cameraIdList ?: emptyArray()
        } catch (_: Exception) {
            emptyArray()
        }

        var hasFront = false
        var hasBack = false
        for (id in cameraIds) {
            try {
                val characteristics = cameraManager?.getCameraCharacteristics(id)
                val facing = characteristics?.get(CameraCharacteristics.LENS_FACING)
                when (facing) {
                    CameraCharacteristics.LENS_FACING_FRONT -> hasFront = true
                    CameraCharacteristics.LENS_FACING_BACK -> hasBack = true
                }
            } catch (e: Exception) {
                Log.w("CameraDetect", "Failed to inspect camera $id: ${e.message}")
            }
        }

        val canUseRealCamera = hasFront || hasBack
        val recommendedMode = when {
            canUseRealCamera -> FallbackMode.REAL_CAMERA
            isEmulator -> FallbackMode.TEST_IMAGE
            else -> FallbackMode.MANUAL_SIMULATION
        }

        return Environment(
            hasFrontCamera = hasFront,
            hasBackCamera = hasBack,
            isEmulator = isEmulator,
            canUseRealCamera = canUseRealCamera,
            recommendedFallback = recommendedMode
        )
    }

    private fun isRunningOnEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT == "google_sdk"
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu"))
    }
}

@Composable
fun NoCameraFallbackScreen(
    environment: CameraEnvironmentDetector.Environment,
    onUseTestImage: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF101622))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🔍", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))

        Text(
            text = "وضع المحاكاة والتحليل التفاعلي",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = if (environment.isEmulator) {
                "أنت تعمل في بيئة محاكاة. يمكنك استخدام نموذج القياس والاختبار البيومتري الافتراضي للتجربة الفورية."
            } else {
                "لم يتم العثور على حساس كاميرا نشط. يمكنك المتابعة بنموذج المحاكاة والتحليل القياسي."
            },
            color = Color(0xFF8A94A6),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onUseTestImage,
            colors = ButtonDefaults.buttonColors(containerColor = BeautyPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("متابعة بالنموذج البيومتري الافتراضي", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("إعادة فحص الكاميرا", color = Color.White)
        }
    }
}
