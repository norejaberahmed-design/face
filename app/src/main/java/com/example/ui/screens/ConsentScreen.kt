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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BeautyBackground
import com.example.ui.theme.BeautyCardBorder
import com.example.ui.theme.BeautyPrimary
import com.example.ui.theme.BeautySurface
import com.example.ui.theme.BeautyText
import com.example.ui.theme.BeautyTextMuted

@Composable
fun ConsentScreen(
    onAgree: () -> Unit
) {
    var agreed by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BeautyBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
            .testTag("consent_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Header Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BeautySurface)
                .border(1.dp, BeautyCardBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PrivacyTip,
                contentDescription = null,
                tint = BeautyPrimary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "الموافقة والخصوصية",
            color = BeautyText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("consent_title")
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "نحن نأخذ خصوصيتك على محمل الجد. باستخدامك لهذا التطبيق، أنت توافق على الشروط والسياسات التالية:",
            color = BeautyTextMuted,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 1: Biometric Data Protection (GDPR / PDPL)
        ConsentSection(
            title = "🔒 حماية البيانات البيومترية",
            body = "يتم معالجة صورتك وملامح وجهك على جهازك (On-Device) ولا تُرسل لأي خادم إلا عند موافقتك الصريحة. نحن نلتزم بالمعايير العالمية للخصوصية بما فيها GDPR و PDPL لحماية بياناتك البيومترية المشفرة."
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Section 2: Medical Disclaimer
        ConsentSection(
            title = "⚕️ إخلاء المسؤولية الطبية",
            body = "Beauty Intelligence هو أداة ذكاء اصطناعي للتوعية التجميلية والعناية اليومية بالبشرة فقط، وليس بديلاً عن الاستشارة أو التشخيص الطبي المتخصص. يُرجى مراجعة طبيب الجلدية المعتمد لأي حالة مرضية."
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Section 3: Camera Usage
        ConsentSection(
            title = "📸 استخدام الكاميرا والمعالم الوجهية",
            body = "نحتاج صلاحية الكاميرا لالتقاط صور وجهك واستخراج 468 نقطة هندسية بدقة 30 إطار/ثانية وتحليلها محلياً. لا نقوم بتخزين أو رفع الصور السحابية إلا بعد إذنك الصريح."
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Agreement Checkbox Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BeautySurface)
                .border(1.dp, if (agreed) BeautyPrimary else BeautyCardBorder, RoundedCornerShape(12.dp))
                .clickable { agreed = !agreed }
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("consent_checkbox_row"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "أوافق على الشروط وسياسة الخصوصية واستخدام البيانات",
                color = BeautyText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (agreed) BeautyPrimary else BeautyBackground)
                    .border(2.dp, BeautyPrimary, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (agreed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "موافق",
                        tint = BeautyBackground,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = onAgree,
            enabled = agreed,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BeautyPrimary,
                contentColor = BeautyBackground,
                disabledContainerColor = BeautySurface,
                disabledContentColor = BeautyTextMuted
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("consent_continue_button")
        ) {
            Text(
                text = "متابعة",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ConsentSection(
    title: String,
    body: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BeautySurface)
            .border(1.dp, BeautyCardBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = BeautyPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = body,
            color = BeautyText,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}
