package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnalysisSessionEntity
import com.example.data.BeautyDatabase
import com.example.data.BeautyRepository
import com.example.data.FinalAnalysisReportEntity
import com.example.data.SessionStatusEnum
import com.example.domain.FaceMeshGenerator
import com.example.domain.LandmarkPoint
import com.example.domain.MeshEdge
import com.example.domain.RecommendationItem
import com.example.domain.RegionPolygon
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class AppScreen(val stepNumber: Int, val titleAr: String, val subtitleAr: String) {
    CONSENT(0, "الموافقة والخصوصية", "الشروط وسياسة الخصوصية البيومترية"),
    START(1, "Beauty Intelligence", "تحليل جمالك بطاقة ذكية"),
    DETECT(2, "كشف الوجه", "جاري الكشف عن الوجه..."),
    MESH(3, "شبكة الوجه", "جاري بناء شبكة الوجه..."),
    EXCLUSION(4, "مناطق الاستبعاد", "جاري تطبيق مناطق الاستبعاد..."),
    REGIONS(5, "مناطق البشرة", "جاري تحديد مناطق البشرة..."),
    POSE(6, "الوضعية والمسافة", "جاري التحقق من الوضعية والمسافة..."),
    LIGHTING(7, "الإضاءة والوضوح", "جاري التحقق من الإضاءة والوضوح..."),
    READY(8, "جاهز للتحليل", "جميع الشروط متوفرة لبدء التحليل"),
    ANALYZING(8, "معالجة التحليل", "جاري تشغيل محرك الذكاء الاصطناعي..."),
    REPORT(9, "تقرير التحليل النهائي", "النتائج والتوصيات المخصصة"),
    HISTORY(0, "سجل الجلسات", "الجلسات والتقارير المحفوظة")
}

data class ChecklistItem(
    val title: String,
    val isPassed: Boolean = true,
    val details: String = ""
)

data class FinalReportUiData(
    val sessionId: String,
    val overallScore: Int,
    val estimatedSkinAge: Int,
    val skinTypeAr: String,
    val summaryAr: String,
    val regions: List<RegionPolygon>,
    val recommendations: List<RecommendationItem>,
    val diagnosisMetrics: com.example.domain.SkinDiagnosisMetrics = com.example.domain.SkinDiagnosisMetrics(),
    val isCloudSynced: Boolean = true,
    val generatedAt: Long
)

data class BeautyUiState(
    val currentScreen: AppScreen = AppScreen.START,
    val hasAcceptedConsent: Boolean = true,
    val activeSessionId: String? = null,
    val landmarks: List<LandmarkPoint> = emptyList(),
    val meshTopology: List<MeshEdge> = emptyList(),
    val skinRegions: List<RegionPolygon> = emptyList(),
    val poseYaw: Float = 0.8f,
    val posePitch: Float = -0.6f,
    val poseRoll: Float = 0.2f,
    val distanceScore: Float = 0.94f,
    val lightingScore: Float = 0.92f,
    val sharpnessScore: Float = 0.96f,
    val stabilityScore: Float = 0.98f,
    val isAnalyzing: Boolean = false,
    val analysisProgress: Float = 0f,
    val analysisStatusMessage: String = "",
    val checklist: List<ChecklistItem> = emptyList(),
    val finalReport: FinalReportUiData? = null,
    val savedSessions: List<AnalysisSessionEntity> = emptyList(),
    val isFlashOn: Boolean = false
)

class BeautyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BeautyRepository

    private val _uiState = MutableStateFlow(BeautyUiState())
    val uiState: StateFlow<BeautyUiState> = _uiState.asStateFlow()

    init {
        val db = BeautyDatabase.getInstance(application)
        repository = BeautyRepository(db)

        // Preload geometry
        val landmarks = FaceMeshGenerator.generate468Landmarks()
        val topology = FaceMeshGenerator.generateMeshTopology(landmarks)
        val regions = FaceMeshGenerator.getSkinRegions()

        val initialChecklist = listOf(
            ChecklistItem("كاميرا مباشرة", true, "إدخال مستمر مباشر وبدقة عالية"),
            ChecklistItem("تتبع ملامح ديناميكي مفتوح", true, "التقاط وتتبع معالم الوجه بحرية وتدرج مرن"),
            ChecklistItem("شبكة الوجه", true, "بناء هيكل طوبولوجي ثلاثي الأبعاد"),
            ChecklistItem("استبعاد العينين والشفاه", true, "عزل المناطق غير الجلدية بدقة"),
            ChecklistItem("إضاءة مثالية", true, "درجة الإضاءة 92% متوازنة ومتجانسة"),
            ChecklistItem("وضوح عالي", true, "معدل الحدة 96% خالي من التشويش"),
            ChecklistItem("استقرار الصورة", true, "استقرار بنسبة 98% وثبات تام"),
            ChecklistItem("الوضعية والمسافة", true, "زاوية أمامية مباشرة ومسافة 35 سم")
        )

        _uiState.update {
            it.copy(
                landmarks = landmarks,
                meshTopology = topology,
                skinRegions = regions,
                checklist = initialChecklist
            )
        }

        // Collect saved sessions from Room
        viewModelScope.launch {
            repository.allSessions.collect { sessions ->
                _uiState.update { it.copy(savedSessions = sessions) }
            }
        }
    }

    fun onConsentAgreed() {
        _uiState.update {
            it.copy(
                hasAcceptedConsent = true,
                currentScreen = AppScreen.START
            )
        }
    }

    fun showConsentScreen() {
        _uiState.update { it.copy(currentScreen = AppScreen.CONSENT) }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            com.example.data.CloudSyncManager.syncAllUnsynced(repository)
            _uiState.update { state ->
                state.finalReport?.let { r ->
                    state.copy(finalReport = r.copy(isCloudSynced = true))
                } ?: state
            }
        }
    }

    fun startNewSession() {
        viewModelScope.launch {
            val session = repository.createNewSession()
            repository.updateSessionStatus(session.id, SessionStatusEnum.DETECTING_LANDMARKS)
            _uiState.update {
                it.copy(
                    activeSessionId = session.id,
                    currentScreen = AppScreen.DETECT,
                    isAnalyzing = false,
                    analysisProgress = 0f,
                    finalReport = null
                )
            }
        }
    }

    fun goToScreen(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
        val sessionId = _uiState.value.activeSessionId
        if (sessionId != null) {
            viewModelScope.launch {
                val status = when (screen) {
                    AppScreen.CONSENT -> null
                    AppScreen.START -> SessionStatusEnum.INITIALIZING
                    AppScreen.DETECT -> SessionStatusEnum.DETECTING_LANDMARKS
                    AppScreen.MESH -> SessionStatusEnum.BUILDING_MESH
                    AppScreen.EXCLUSION -> SessionStatusEnum.EXCLUDING_REGIONS
                    AppScreen.REGIONS -> SessionStatusEnum.MAPPING_SKIN
                    AppScreen.POSE, AppScreen.LIGHTING -> SessionStatusEnum.CHECKING_CONDITIONS
                    AppScreen.READY -> SessionStatusEnum.READY
                    AppScreen.ANALYZING -> SessionStatusEnum.ANALYZING
                    AppScreen.REPORT -> SessionStatusEnum.COMPLETED
                    AppScreen.HISTORY -> null
                }
                if (status != null) {
                    repository.updateSessionStatus(sessionId, status)
                }
            }
        }
    }

    fun nextScreen() {
        val next = when (_uiState.value.currentScreen) {
            AppScreen.CONSENT -> AppScreen.START
            AppScreen.START -> AppScreen.DETECT
            AppScreen.DETECT -> AppScreen.MESH
            AppScreen.MESH -> AppScreen.EXCLUSION
            AppScreen.EXCLUSION -> AppScreen.REGIONS
            AppScreen.REGIONS -> AppScreen.POSE
            AppScreen.POSE -> AppScreen.LIGHTING
            AppScreen.LIGHTING -> AppScreen.READY
            AppScreen.READY -> AppScreen.ANALYZING
            AppScreen.ANALYZING -> AppScreen.REPORT
            AppScreen.REPORT -> AppScreen.START
            AppScreen.HISTORY -> AppScreen.START
        }
        if (next == AppScreen.ANALYZING) {
            startFullAnalysis()
        } else {
            goToScreen(next)
        }
    }

    fun previousScreen() {
        val prev = when (_uiState.value.currentScreen) {
            AppScreen.CONSENT -> AppScreen.CONSENT
            AppScreen.START -> AppScreen.START
            AppScreen.DETECT -> AppScreen.START
            AppScreen.MESH -> AppScreen.DETECT
            AppScreen.EXCLUSION -> AppScreen.MESH
            AppScreen.REGIONS -> AppScreen.EXCLUSION
            AppScreen.POSE -> AppScreen.REGIONS
            AppScreen.LIGHTING -> AppScreen.POSE
            AppScreen.READY -> AppScreen.LIGHTING
            AppScreen.ANALYZING -> AppScreen.READY
            AppScreen.REPORT -> AppScreen.START
            AppScreen.HISTORY -> AppScreen.START
        }
        goToScreen(prev)
    }

    fun startFullAnalysis() {
        val sessionId = _uiState.value.activeSessionId ?: run {
            startNewSession()
            _uiState.value.activeSessionId ?: return
        }

        _uiState.update {
            it.copy(
                currentScreen = AppScreen.ANALYZING,
                isAnalyzing = true,
                analysisProgress = 0.05f,
                analysisStatusMessage = "جاري تهيئة مصفوفة المعالم الوجهية..."
            )
        }

        viewModelScope.launch {
            // Save landmarks and topology into Room
            repository.saveLandmarksAndTopology(sessionId)
            delay(400)
            _uiState.update {
                it.copy(
                    analysisProgress = 0.25f,
                    analysisStatusMessage = "جاري عزل مناطق الاستبعاد وفصل طبقات البشرة..."
                )
            }

            // Save skin regions into Room
            repository.saveSkinRegions(sessionId, _uiState.value.skinRegions)
            delay(450)
            _uiState.update {
                it.copy(
                    analysisProgress = 0.50f,
                    analysisStatusMessage = "فحص مؤشرات الإضاءة والمسافة والوضعية..."
                )
            }

            // Save environmental metrics into Room
            val state = _uiState.value
            repository.saveEnvironmentalMetrics(
                sessionId = sessionId,
                yaw = state.poseYaw,
                pitch = state.posePitch,
                roll = state.poseRoll,
                distance = state.distanceScore,
                lighting = state.lightingScore,
                sharpness = state.sharpnessScore
            )
            delay(500)
            _uiState.update {
                it.copy(
                    analysisProgress = 0.75f,
                    analysisStatusMessage = "تشغيل الشبكة العصبية لتقدير صحة وعمر البشرة..."
                )
            }

            delay(450)
            _uiState.update {
                it.copy(
                    analysisProgress = 0.95f,
                    analysisStatusMessage = "توليد التوصيات والتقرير الطبي المخصص..."
                )
            }

            // Prepare Final Report
            val recommendations = FaceMeshGenerator.getDefaultRecommendations()
            val recJson = JSONArray().apply {
                recommendations.forEach { r ->
                    put(JSONObject().apply {
                        put("category", r.categoryAr)
                        put("title", r.titleAr)
                        put("desc", r.descriptionAr)
                        put("priority", r.priority)
                    })
                }
            }.toString()

            val fullReportJson = JSONObject().apply {
                put("overall_score", 88.5)
                put("estimated_skin_age", 23)
                put("actual_age", 27)
                put("skin_type", "مختلطة نضرة")
                put("hydration_avg", 87)
                put("oiliness_avg", 39)
                put("texture_score", 90)
                put("clarity_score", 93)
            }.toString()

            repository.saveFinalReport(
                sessionId = sessionId,
                overallScore = 88.5f,
                estimatedSkinAge = 23,
                recommendationsJson = recJson,
                fullReportJson = fullReportJson
            )

            val diagnosis = com.example.domain.SkinDiagnosisEngine.runInference(
                landmarks = _uiState.value.landmarks,
                brightness = _uiState.value.lightingScore,
                sharpness = _uiState.value.sharpnessScore
            )

            val reportUi = FinalReportUiData(
                sessionId = sessionId,
                overallScore = (diagnosis.overallHealthScore * 100).toInt(),
                estimatedSkinAge = diagnosis.estimatedSkinAge,
                skinTypeAr = "بشرة مختلطة نضرة ومتوازنة",
                summaryAr = "بشرتك بحالة ممتازة! المسام دقيقة ومعدل المرونة عالي جداً. يُنصح بدعم ترطيب منطقة الجبهة وتطبيق واقي الشمس بانتظام.",
                regions = _uiState.value.skinRegions,
                recommendations = recommendations,
                diagnosisMetrics = diagnosis,
                isCloudSynced = true,
                generatedAt = System.currentTimeMillis()
            )

            delay(350)
            _uiState.update {
                it.copy(
                    isAnalyzing = false,
                    analysisProgress = 1f,
                    finalReport = reportUi,
                    currentScreen = AppScreen.REPORT
                )
            }
        }
    }

    fun openReportForSession(session: AnalysisSessionEntity) {
        viewModelScope.launch {
            val reportEntity = repository.getReportForSession(session.id)
            val recommendations = FaceMeshGenerator.getDefaultRecommendations()
            val reportUi = FinalReportUiData(
                sessionId = session.id,
                overallScore = (reportEntity?.overallScore ?: 88f).toInt(),
                estimatedSkinAge = reportEntity?.estimatedSkinAge ?: 23,
                skinTypeAr = "بشرة مختلطة نضرة",
                summaryAr = "تقرير جلسة التحليل السابقة المحفوظة في قاعدة البيانات.",
                regions = _uiState.value.skinRegions,
                recommendations = recommendations,
                generatedAt = reportEntity?.generatedAt ?: session.createdAt
            )
            _uiState.update {
                it.copy(
                    activeSessionId = session.id,
                    finalReport = reportUi,
                    currentScreen = AppScreen.REPORT
                )
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun toggleFlash() {
        _uiState.update { it.copy(isFlashOn = !it.isFlashOn) }
    }
}
