package com.example.camera

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

sealed class CameraPermissionState {
    object Granted : CameraPermissionState()
    object NotRequested : CameraPermissionState()
    object DeniedFirstTime : CameraPermissionState()
    object DeniedPermanently : CameraPermissionState()
}

class CameraPermissionManager(private val activity: ComponentActivity) {

    fun getCurrentState(): CameraPermissionState {
        val granted = ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) return CameraPermissionState.Granted

        return if (!hasRequestedBefore()) {
            CameraPermissionState.NotRequested
        } else {
            val canShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                android.Manifest.permission.CAMERA
            )
            if (canShowRationale) {
                CameraPermissionState.DeniedFirstTime
            } else {
                CameraPermissionState.DeniedPermanently
            }
        }
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    private fun hasRequestedBefore(): Boolean {
        val prefs = activity.getSharedPreferences("camera_perms", Context.MODE_PRIVATE)
        return prefs.getBoolean("requested", false)
    }

    fun markRequested() {
        val prefs = activity.getSharedPreferences("camera_perms", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("requested", true).apply()
    }
}

@Composable
fun CameraPermissionCard(
    state: CameraPermissionState,
    onRequest: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151A25))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📷", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))

            Text(
                text = when (state) {
                    is CameraPermissionState.NotRequested -> "نحتاج للوصول إلى الكاميرا"
                    is CameraPermissionState.DeniedFirstTime -> "إذن الكاميرا مطلوب"
                    is CameraPermissionState.DeniedPermanently -> "تم رفض الصلاحية بشكل دائم"
                    else -> "صلاحية الكاميرا"
                },
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = when (state) {
                    is CameraPermissionState.NotRequested ->
                        "لتتبع ملامح الوجه وتوفير تشخيص ذكي للبشرة في الوقت الفعلي"
                    is CameraPermissionState.DeniedFirstTime ->
                        "يرجى منح الإذن لتفعيل الكاميرا المباشرة والتحليل البيومتري"
                    is CameraPermissionState.DeniedPermanently ->
                        "يرجى فتح إعدادات التطبيق وتفعيل صلاحية الكاميرا للمتابعة"
                    else -> ""
                },
                color = Color(0xFF8A94A6),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = when (state) {
                    is CameraPermissionState.DeniedPermanently -> onOpenSettings
                    else -> onRequest
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5A0)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = when (state) {
                        is CameraPermissionState.DeniedPermanently -> "فتح إعدادات الجهاز"
                        is CameraPermissionState.DeniedFirstTime -> "إعادة المحاولة والمنح"
                        else -> "منح إذن الكاميرا الآن"
                    },
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
