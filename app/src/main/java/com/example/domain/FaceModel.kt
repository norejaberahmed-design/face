package com.example.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

data class LandmarkPoint(
    val id: Int,
    val x: Float, // Normalized 0.0 .. 1.0
    val y: Float, // Normalized 0.0 .. 1.0
    val z: Float = 0f,
    val zone: String = "face"
)

data class MeshEdge(
    val start: Int,
    val end: Int
)

data class RegionPolygon(
    val id: String,
    val nameAr: String,
    val colorHex: String,
    val points: List<Offset>, // Normalized 0.0 .. 1.0
    val score: Int,
    val hydration: Int,
    val oiliness: Int,
    val texture: String,
    val issues: List<String>
)

data class RecommendationItem(
    val categoryAr: String,
    val titleAr: String,
    val descriptionAr: String,
    val iconName: String,
    val priority: String // "عالية", "متوسطة", "روتين يومي"
)

object FaceMeshGenerator {

    /**
     * Generates 468 realistic normalized facial landmarks based on human facial anthropometry.
     */
    fun generate468Landmarks(): List<LandmarkPoint> {
        val points = mutableListOf<LandmarkPoint>()
        var idCounter = 0

        // 1. Jawline & Face Oval (36 points)
        for (i in 0 until 36) {
            val angle = Math.PI * (0.05 + 0.9 * (i / 35.0))
            val cx = 0.5f
            val cy = 0.52f
            val rx = 0.34f
            val ry = 0.40f
            val x = (cx + rx * cos(angle + Math.PI / 2)).toFloat()
            val y = (cy + ry * sin(angle + Math.PI / 2)).toFloat()
            points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "jawline"))
        }

        // 2. Forehead boundary & grid (50 points)
        for (row in 0..4) {
            val y = 0.16f + row * 0.04f
            val countInRow = 10
            for (col in 0 until countInRow) {
                val t = col.toFloat() / (countInRow - 1)
                val x = 0.24f + t * 0.52f + (if (row % 2 == 1) 0.02f else 0f)
                points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "forehead"))
            }
        }

        // 3. Left Eyebrow (18 points)
        for (i in 0 until 18) {
            val t = i / 17.0f
            val x = 0.22f + t * 0.22f
            val y = 0.36f - sin(t * Math.PI.toFloat()) * 0.04f
            points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "left_eyebrow"))
        }

        // 4. Right Eyebrow (18 points)
        for (i in 0 until 18) {
            val t = i / 17.0f
            val x = 0.56f + t * 0.22f
            val y = 0.36f - sin(t * Math.PI.toFloat()) * 0.04f
            points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "right_eyebrow"))
        }

        // 5. Left Eye Outer & Inner Ring (32 points)
        for (i in 0 until 32) {
            val angle = 2 * Math.PI * (i / 32.0)
            val cx = 0.33f
            val cy = 0.43f
            val rx = if (i < 16) 0.075f else 0.045f
            val ry = if (i < 16) 0.038f else 0.022f
            val x = (cx + rx * cos(angle)).toFloat()
            val y = (cy + ry * sin(angle)).toFloat()
            points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "left_eye"))
        }

        // 6. Right Eye Outer & Inner Ring (32 points)
        for (i in 0 until 32) {
            val angle = 2 * Math.PI * (i / 32.0)
            val cx = 0.67f
            val cy = 0.43f
            val rx = if (i < 16) 0.075f else 0.045f
            val ry = if (i < 16) 0.038f else 0.022f
            val x = (cx + rx * cos(angle)).toFloat()
            val y = (cy + ry * sin(angle)).toFloat()
            points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "right_eye"))
        }

        // 7. Nose Bridge, Tip & Wings (42 points)
        // Bridge
        for (i in 0 until 12) {
            val t = i / 11.0f
            val y = 0.38f + t * 0.20f
            val x = 0.50f + (if (i % 2 == 1) 0.015f else -0.015f)
            points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "nose_bridge"))
        }
        // Tip & nostrils
        for (i in 0 until 30) {
            val angle = Math.PI * (i / 29.0)
            val cx = 0.50f
            val cy = 0.58f
            val rx = 0.08f
            val ry = 0.05f
            val x = (cx + rx * cos(angle)).toFloat()
            val y = (cy + ry * sin(angle)).toFloat()
            points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "nose_tip"))
        }

        // 8. Lips Outer & Inner Contour (40 points)
        for (i in 0 until 40) {
            val angle = 2 * Math.PI * (i / 40.0)
            val cx = 0.50f
            val cy = 0.70f
            val rx = if (i < 20) 0.12f else 0.08f
            val ry = if (i < 20) 0.055f else 0.03f
            val x = (cx + rx * cos(angle)).toFloat()
            val y = (cy + ry * sin(angle)).toFloat()
            points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "lips"))
        }

        // 9. Left Cheek Lattice (50 points)
        for (r in 0 until 5) {
            for (c in 0 until 10) {
                val x = 0.20f + (c / 9.0f) * 0.18f
                val y = 0.49f + (r / 4.0f) * 0.19f
                points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "left_cheek"))
            }
        }

        // 10. Right Cheek Lattice (50 points)
        for (r in 0 until 5) {
            for (c in 0 until 10) {
                val x = 0.62f + (c / 9.0f) * 0.18f
                val y = 0.49f + (r / 4.0f) * 0.19f
                points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "right_cheek"))
            }
        }

        // 11. Chin & Submental zone (48 points)
        for (r in 0 until 4) {
            for (c in 0 until 12) {
                val x = 0.38f + (c / 11.0f) * 0.24f
                val y = 0.77f + (r / 3.0f) * 0.14f
                points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "chin"))
            }
        }

        // Fill remaining up to exactly 468 points with smooth facial contour fillers
        val remaining = 468 - points.size
        for (i in 0 until remaining) {
            val t = i.toFloat() / remaining
            val angle = 2 * Math.PI * t
            val r = 0.18f + 0.12f * (i % 3) / 2.0f
            val x = (0.50f + r * cos(angle)).toFloat()
            val y = (0.50f + r * 1.25f * sin(angle)).toFloat()
            points.add(LandmarkPoint(id = idCounter++, x = x, y = y, zone = "temple"))
        }

        return points.take(468)
    }

    /**
     * Generates facial mesh wireframe topology edges connecting adjacent landmarks.
     */
    fun generateMeshTopology(landmarks: List<LandmarkPoint>): List<MeshEdge> {
        val edges = mutableListOf<MeshEdge>()
        val maxPoints = landmarks.size

        // Connect along consecutive landmarks in same zone
        for (i in 0 until maxPoints - 1) {
            if (landmarks[i].zone == landmarks[i + 1].zone) {
                edges.add(MeshEdge(i, i + 1))
            }
        }

        // Triangulate across proximity
        for (i in 0 until maxPoints step 2) {
            val p1 = landmarks[i]
            for (j in (i + 1) until minOf(i + 14, maxPoints)) {
                val p2 = landmarks[j]
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val distSq = dx * dx + dy * dy
                if (distSq < 0.005f) { // Closely spaced points
                    edges.add(MeshEdge(i, j))
                }
            }
        }

        return edges
    }

    /**
     * Predefined skin regions with polygon coordinates and analytical metrics.
     */
    fun getSkinRegions(): List<RegionPolygon> {
        return listOf(
            RegionPolygon(
                id = "forehead",
                nameAr = "منطقة الجبهة",
                colorHex = "#FFD700", // Yellow
                points = listOf(
                    Offset(0.24f, 0.17f),
                    Offset(0.50f, 0.14f),
                    Offset(0.76f, 0.17f),
                    Offset(0.78f, 0.32f),
                    Offset(0.50f, 0.34f),
                    Offset(0.22f, 0.32f)
                ),
                score = 86,
                hydration = 88,
                oiliness = 42,
                texture = "ناعم ومتجانس",
                issues = listOf("ترطيب مثالي", "خطوط تعبيرية طفيفة")
            ),
            RegionPolygon(
                id = "left_cheek",
                nameAr = "الخد الأيسر",
                colorHex = "#4D79FF", // Blue
                points = listOf(
                    Offset(0.18f, 0.46f),
                    Offset(0.38f, 0.46f),
                    Offset(0.40f, 0.66f),
                    Offset(0.24f, 0.68f),
                    Offset(0.16f, 0.58f)
                ),
                score = 91,
                hydration = 92,
                oiliness = 34,
                texture = "مرونة ممتازة",
                issues = listOf("حاجز دهني صحي", "خالي من التصبغات")
            ),
            RegionPolygon(
                id = "right_cheek",
                nameAr = "الخد الأيمن",
                colorHex = "#B84DFF", // Purple
                points = listOf(
                    Offset(0.62f, 0.46f),
                    Offset(0.82f, 0.46f),
                    Offset(0.84f, 0.58f),
                    Offset(0.76f, 0.68f),
                    Offset(0.60f, 0.66f)
                ),
                score = 89,
                hydration = 89,
                oiliness = 36,
                texture = "نضارة ملحوظة",
                issues = listOf("مسام غير ملحوظة", "لون موحد")
            ),
            RegionPolygon(
                id = "t_zone",
                nameAr = "الأنف ومنطقة T",
                colorHex = "#00D2FF", // Cyan
                points = listOf(
                    Offset(0.44f, 0.36f),
                    Offset(0.56f, 0.36f),
                    Offset(0.58f, 0.62f),
                    Offset(0.50f, 0.65f),
                    Offset(0.42f, 0.62f)
                ),
                score = 83,
                hydration = 78,
                oiliness = 58,
                texture = "إفراز زهمي خفيف",
                issues = listOf("نشاط دهني معتدل", "مسام واضحة قليلاً")
            ),
            RegionPolygon(
                id = "chin",
                nameAr = "الذقن ومحيط الفك",
                colorHex = "#00E5A0", // Green
                points = listOf(
                    Offset(0.36f, 0.77f),
                    Offset(0.64f, 0.77f),
                    Offset(0.66f, 0.88f),
                    Offset(0.50f, 0.92f),
                    Offset(0.34f, 0.88f)
                ),
                score = 88,
                hydration = 84,
                oiliness = 38,
                texture = "تماسك طبيعي",
                issues = listOf("خالي من الحبوب", "شد طبيعي")
            )
        )
    }

    /**
     * Exclusion zones: Left Eye, Right Eye, Eyebrows, Lips
     */
    fun getExclusionZones(): List<List<Offset>> {
        return listOf(
            // Left Eye
            listOf(
                Offset(0.25f, 0.43f),
                Offset(0.33f, 0.39f),
                Offset(0.41f, 0.43f),
                Offset(0.33f, 0.47f)
            ),
            // Right Eye
            listOf(
                Offset(0.59f, 0.43f),
                Offset(0.67f, 0.39f),
                Offset(0.75f, 0.43f),
                Offset(0.67f, 0.47f)
            ),
            // Lips
            listOf(
                Offset(0.38f, 0.70f),
                Offset(0.50f, 0.65f),
                Offset(0.62f, 0.70f),
                Offset(0.50f, 0.75f)
            )
        )
    }

    /**
     * Clinical skincare recommendations based on deep analysis
     */
    fun getDefaultRecommendations(): List<RecommendationItem> {
        return listOf(
            RecommendationItem(
                categoryAr = "العناية اليومية (AM)",
                titleAr = "سيروم حمض الهيالورونيك مع فيتامين B5",
                descriptionAr = "لتعزيز الترطيب العميق في منطقة الجبهة والخدين بنسبة +24% والحفاظ على مرونة الأنسجة.",
                iconName = "water_drop",
                priority = "عالية"
            ),
            RecommendationItem(
                categoryAr = "الحماية الشمسية",
                titleAr = "واقي شمس واسع المدى SPF 50+ خفيف القوام",
                descriptionAr = "حماية حاجز البشرة من الأشعة فوق البنفسجية UV لمنع التصبغات والحفاظ على شباب الجلد.",
                iconName = "wb_sunny",
                priority = "عالية"
            ),
            RecommendationItem(
                categoryAr = "تنظيم الزهم (T-Zone)",
                titleAr = "تونر النياسيناميد 5% والزنك",
                descriptionAr = "لتقليص مظهر المسام في منطقة T وتنظيم الإفرازات الزيتية لمنح مظهر مات غير لامع.",
                iconName = "tune",
                priority = "متوسطة"
            ),
            RecommendationItem(
                categoryAr = "الترميم الليلي (PM)",
                titleAr = "كريم السيراميد والببتيدات التجديدي",
                descriptionAr = "تقوية طبقة الحماية السطحية وتجديد خلايا البشرة أثناء النوم لمقاومة الإجهاد البيئي.",
                iconName = "nights_stay",
                priority = "روتين يومي"
            )
        )
    }
}
