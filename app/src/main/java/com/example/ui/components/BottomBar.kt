package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BeautyBackground
import com.example.ui.theme.BeautyPrimary
import com.example.ui.theme.BeautyText
import com.example.ui.theme.BeautyTextMuted

@Composable
fun BeautyBottomBar(
    allConditionsPassed: Boolean = true,
    onGalleryClick: () -> Unit = {},
    onCaptureClick: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BeautyBackground)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 10.dp)
            .testTag("bottom_bar"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onGalleryClick,
                modifier = Modifier.testTag("bottom_gallery_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Collections,
                    contentDescription = "معرض الصور والسجلات",
                    tint = BeautyText,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Circular capture button matching React Native LiveCameraScreen:
            // captureBtn: width: 76, height: 76, borderRadius: 38, borderWidth: 4
            // captureInner: width: 56, height: 56, borderRadius: 28
            val borderColor = if (allConditionsPassed) BeautyPrimary else BeautyTextMuted
            val innerColor = if (allConditionsPassed) BeautyPrimary else BeautyTextMuted.copy(alpha = 0.6f)

            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .border(width = 4.dp, color = borderColor, shape = CircleShape)
                    .clickable(enabled = allConditionsPassed) { onCaptureClick() }
                    .testTag("capture_button"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(innerColor)
                )
            }

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("bottom_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = "قائمة الشروط والمعايير",
                    tint = BeautyText,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Hint text matching React Native spec
        Text(
            text = if (allConditionsPassed) "جاهز للتصوير والتحليل ✅" else "جاري التحقق من الشروط...",
            color = if (allConditionsPassed) BeautyPrimary else BeautyTextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

