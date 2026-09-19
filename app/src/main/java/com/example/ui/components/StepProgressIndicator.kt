package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.theme.BeautyCardBorder
import com.example.ui.theme.BeautyPrimary
import com.example.ui.theme.BeautySurface
import com.example.ui.theme.BeautyText
import com.example.ui.theme.BeautyTextMuted

@Composable
fun StepProgressIndicator(
    currentScreen: AppScreen,
    onStepClick: (AppScreen) -> Unit
) {
    val steps = listOf(
        AppScreen.START to "1. البداية",
        AppScreen.DETECT to "2. كشف",
        AppScreen.MESH to "3. شبكة",
        AppScreen.EXCLUSION to "4. استبعاد",
        AppScreen.REGIONS to "5. مناطق",
        AppScreen.POSE to "6. وضعية",
        AppScreen.LIGHTING to "7. إضاءة",
        AppScreen.READY to "8. جاهز"
    )

    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BeautySurface)
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .horizontalScroll(scrollState)
            .testTag("step_progress_indicator"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEach { (screen, label) ->
            val isSelected = currentScreen == screen
            val isPast = currentScreen.stepNumber > screen.stepNumber

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        when {
                            isSelected -> BeautyPrimary
                            isPast -> BeautyCardBorder
                            else -> Color.Transparent
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) BeautyPrimary else BeautyCardBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onStepClick(screen) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> Color(0xFF0A0E17)
                        isPast -> BeautyText
                        else -> BeautyTextMuted
                    }
                )
            }
        }
    }
}
