package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.RecommendationItem
import com.example.domain.RegionPolygon
import com.example.ui.FinalReportUiData
import com.example.ui.components.BeautyHeader
import com.example.ui.theme.BeautyBackground
import com.example.ui.theme.BeautyBlue
import com.example.ui.theme.BeautyCardBorder
import com.example.ui.theme.BeautyCyan
import com.example.ui.theme.BeautyPrimary
import com.example.ui.theme.BeautyPurple
import com.example.ui.theme.BeautySurface
import com.example.ui.theme.BeautySurfaceVariant
import com.example.ui.theme.BeautyText
import com.example.ui.theme.BeautyTextMuted
import com.example.ui.theme.BeautyYellow

@Composable
fun ReportScreen(
    report: FinalReportUiData,
    onBackToHome: () -> Unit,
    onRetakeScan: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeautyBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("report_screen")
    ) {
        BeautyHeader(
            title = "تقرير الجمال النهائي",
            showBackButton = true,
            onBackClick = onBackToHome,
            onHistoryClick = onHistoryClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Success Icon matching React Native specification:
            // Ionicons name="checkmark-circle" size={100} color={COLORS.primary}
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(BeautySurface)
                    .border(width = 2.dp, color = BeautyPrimary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "تم بنجاح",
                    tint = BeautyPrimary,
                    modifier = Modifier.size(62.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "تم التحليل بنجاح!",
                color = BeautyText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = report.summaryAr,
                color = BeautyTextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Score & Skin Age Overview Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Overall Score Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BeautySurface)
                        .border(width = 1.dp, color = BeautyPrimary.copy(alpha = 0.5f), shape = RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("درجة صحة البشرة", color = BeautyTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "${report.overallScore}",
                        color = BeautyPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("من 100 • ممتازة", color = BeautyTextMuted, fontSize = 10.sp)
                }

                // Estimated Skin Age Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BeautySurface)
                        .border(width = 1.dp, color = BeautyCyan.copy(alpha = 0.5f), shape = RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("عمر البشرة التقديري", color = BeautyTextMuted, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "${report.estimatedSkinAge} سنة",
                        color = BeautyCyan,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("أصغر من العمر الزمني", color = BeautyTextMuted, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cloud Sync Status Card (Step 6)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BeautySurface)
                    .border(1.dp, BeautyCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("☁️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (report.isCloudSynced) "المزامنة السحابية: متزامن ✅" else "المزامنة السحابية: قيد المزامنة...",
                            color = if (report.isCloudSynced) BeautyPrimary else BeautyYellow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تم تشفير ونقل البيانات وفق معايير GDPR و PDPL",
                            color = BeautyTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // On-Device AI Skin Diagnosis Panel (Step 4: skinDiagnosis.ts)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BeautySurface)
                    .border(1.dp, BeautyCardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧠 تشخيص الذكاء الاصطناعي (TFLite On-Device)",
                        color = BeautyText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "مباشر محلي",
                        color = BeautyPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val metrics = report.diagnosisMetrics
                DiagnosisIndicatorRow("حب الشباب (Acne)", (metrics.acneScore * 100).toInt(), isInverse = true)
                Spacer(modifier = Modifier.height(8.dp))
                DiagnosisIndicatorRow("التصبغات (Pigmentation)", (metrics.pigmentationScore * 100).toInt(), isInverse = true)
                Spacer(modifier = Modifier.height(8.dp))
                DiagnosisIndicatorRow("المسام (Pores)", (metrics.poresScore * 100).toInt(), isInverse = true)
                Spacer(modifier = Modifier.height(8.dp))
                DiagnosisIndicatorRow("التجاعيد (Wrinkles)", (metrics.wrinklesScore * 100).toInt(), isInverse = true)
                Spacer(modifier = Modifier.height(8.dp))
                DiagnosisIndicatorRow("الاحمرار (Redness)", (metrics.rednessScore * 100).toInt(), isInverse = true)
                Spacer(modifier = Modifier.height(8.dp))
                DiagnosisIndicatorRow("الترطيب (Moisture)", (metrics.moistureScore * 100).toInt(), isInverse = false)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Regional Breakdown Section
            Text(
                text = "تحليل مناطق البشرة بالتفصيل",
                color = BeautyText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                report.regions.forEach { region ->
                    RegionDetailCard(region)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recommendations Section
            Text(
                text = "التوصيات الطبية والروتين اليومي",
                color = BeautyText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                report.recommendations.forEach { rec ->
                    RecommendationCard(rec)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = onBackToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("report_back_home_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BeautyPrimary,
                    contentColor = BeautyBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "العودة للرئيسية",
                    color = BeautyBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onRetakeScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("report_rescan_button"),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BeautyText),
                border = androidx.compose.foundation.BorderStroke(1.dp, BeautyCardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = BeautyPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("إجراء فحص جديد", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RegionDetailCard(region: RegionPolygon) {
    val accentColor = when (region.id) {
        "forehead" -> BeautyYellow
        "left_cheek" -> BeautyBlue
        "right_cheek" -> BeautyPurple
        "t_zone" -> BeautyCyan
        "chin" -> BeautyPrimary
        else -> BeautyPrimary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BeautySurface)
            .border(width = 1.dp, color = BeautyCardBorder, shape = RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = region.nameAr,
                    color = BeautyText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${region.score}%",
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Metrics: Hydration and Oiliness
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الترطيب", color = BeautyTextMuted, fontSize = 11.sp)
                    Text("${region.hydration}%", color = BeautyText, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { region.hydration / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BeautyPrimary,
                    trackColor = BeautySurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الدهون (الزهم)", color = BeautyTextMuted, fontSize = 11.sp)
                    Text("${region.oiliness}%", color = BeautyText, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { region.oiliness / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BeautyYellow,
                    trackColor = BeautySurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "الملمس: ${region.texture} • ${region.issues.joinToString("، ")}",
            color = BeautyTextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun RecommendationCard(item: RecommendationItem) {
    val icon = when (item.iconName) {
        "water_drop" -> Icons.Default.WaterDrop
        "wb_sunny" -> Icons.Default.WbSunny
        "tune" -> Icons.Default.Tune
        else -> Icons.Default.NightsStay
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BeautySurface)
            .border(width = 1.dp, color = BeautyCardBorder, shape = RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BeautySurfaceVariant)
                .border(width = 1.dp, color = BeautyPrimary.copy(alpha = 0.4f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BeautyPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.titleAr,
                    color = BeautyText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(BeautySurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.priority,
                        color = BeautyPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.descriptionAr,
                color = BeautyTextMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun DiagnosisIndicatorRow(
    label: String,
    scorePercent: Int,
    isInverse: Boolean // For acne/pigmentation, low score is good; for moisture, high score is good
) {
    val isGood = if (isInverse) scorePercent <= 25 else scorePercent >= 70
    val activeColor = if (isGood) BeautyPrimary else BeautyYellow

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = BeautyText,
            fontSize = 12.sp,
            modifier = Modifier.width(140.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(BeautySurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(scorePercent / 100f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(activeColor)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = "$scorePercent%",
            color = activeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}

