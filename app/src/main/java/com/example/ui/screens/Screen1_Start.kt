package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AnalysisSessionEntity
import com.example.ui.theme.BeautyBackground
import com.example.ui.theme.BeautyCardBorder
import com.example.ui.theme.BeautyPrimary
import com.example.ui.theme.BeautySurface
import com.example.ui.theme.BeautySurfaceVariant
import com.example.ui.theme.BeautyText
import com.example.ui.theme.BeautyTextMuted

data class FeatureItemData(
    val icon: ImageVector,
    val label: String
)

@Composable
fun Screen1_Start(
    savedSessions: List<AnalysisSessionEntity>,
    onStartClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onConsentClick: () -> Unit = {}
) {
    // 6 features strictly matching the user's React Native spec:
    // كاميرا مباشرة, 468 نقطة مميزة, شبكة الوجه, استبعاد العينين والشفاه, إضاءة مثالية, وضوح واستقرار
    val features = listOf(
        FeatureItemData(Icons.Default.CameraAlt, "كاميرا مباشرة"),
        FeatureItemData(Icons.Default.CenterFocusStrong, "تتبع ملامح ديناميكي مفتوح"),
        FeatureItemData(Icons.Default.GridOn, "شبكة الوجه"),
        FeatureItemData(Icons.Default.VisibilityOff, "استبعاد العينين والشفاه"),
        FeatureItemData(Icons.Default.WbSunny, "إضاءة مثالية"),
        FeatureItemData(Icons.Default.Speed, "وضوح واستقرار")
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeautyBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState)
            .testTag("screen_start"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Logo Container: scan-circle-outline icon, Title, Subtitle
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(BeautySurface)
                .border(width = 2.dp, color = BeautyPrimary, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CenterFocusStrong,
                contentDescription = "Beauty Intelligence Logo",
                tint = BeautyPrimary,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Beauty Intelligence",
            color = BeautyText,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "تحليل جمالك بطاقة ذكية",
            color = BeautyTextMuted,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Features Grid (2 columns x 3 rows) matching React Native spec
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(BeautySurface)
                .border(width = 1.dp, color = BeautyCardBorder, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            for (row in 0 until 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0 until 2) {
                        val index = row * 2 + col
                        if (index < features.size) {
                            val item = features[index]
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(CircleShape)
                                        .background(BeautySurfaceVariant)
                                        .border(width = 1.dp, color = BeautyCardBorder, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = BeautyPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = item.label,
                                    color = BeautyTextMuted,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start Button matching React Native spec:
        // backgroundColor: primary, padding: 18, borderRadius: 12, gap: 10, text: "إبدأ الآن"
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = BeautyPrimary,
                contentColor = BeautyBackground
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = BeautyBackground,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "إبدأ الآن",
                    color = BeautyBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (savedSessions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onHistoryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("view_history_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BeautyText
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BeautyCardBorder)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = BeautyPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "سجل الجلسات السابقة (${savedSessions.size})",
                        color = BeautyText,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & Consent Link (Step 7)
        Row(
            modifier = Modifier
                .clickable { onConsentClick() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔒 الشروط وسياسة الخصوصية وحماية البيانات البيومترية",
                color = BeautyTextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
