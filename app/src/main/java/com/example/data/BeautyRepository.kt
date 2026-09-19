package com.example.data

import com.example.domain.FaceMeshGenerator
import com.example.domain.RegionPolygon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class BeautyRepository(private val database: BeautyDatabase) {

    val allSessions: Flow<List<AnalysisSessionEntity>> =
        database.analysisSessionDao().getAllSessions()

    val allReports: Flow<List<FinalAnalysisReportEntity>> =
        database.finalAnalysisReportDao().getAllReports()

    suspend fun createNewSession(): AnalysisSessionEntity = withContext(Dispatchers.IO) {
        val session = AnalysisSessionEntity(
            id = UUID.randomUUID().toString(),
            status = SessionStatusEnum.INITIALIZING.value,
            aiModelVersion = "FaceMesh-v2.4-SkinNet",
            deviceInfo = JSONObject().apply {
                put("platform", "Android")
                put("sensor_type", "Biometric Front Camera")
                put("resolution", "1080x1920")
                put("ai_accelerator", "NNAPI-Vulkan")
            }.toString(),
            createdAt = System.currentTimeMillis()
        )
        database.analysisSessionDao().insertSession(session)
        session
    }

    suspend fun updateSessionStatus(sessionId: String, status: SessionStatusEnum) = withContext(Dispatchers.IO) {
        val existing = database.analysisSessionDao().getSessionById(sessionId)
        if (existing != null) {
            val updated = existing.copy(
                status = status.value,
                completedAt = if (status == SessionStatusEnum.COMPLETED) System.currentTimeMillis() else existing.completedAt
            )
            database.analysisSessionDao().updateSession(updated)
        }
    }

    suspend fun saveLandmarksAndTopology(sessionId: String) = withContext(Dispatchers.IO) {
        val landmarks = FaceMeshGenerator.generate468Landmarks()
        val topology = FaceMeshGenerator.generateMeshTopology(landmarks)

        val landmarksArray = JSONArray()
        landmarks.forEach { pt ->
            landmarksArray.put(JSONObject().apply {
                put("id", pt.id)
                put("x", pt.x)
                put("y", pt.y)
                put("z", pt.z)
                put("zone", pt.zone)
            })
        }

        val topologyArray = JSONArray()
        topology.take(300).forEach { edge ->
            topologyArray.put(JSONObject().apply {
                put("start", edge.start)
                put("end", edge.end)
            })
        }

        val entity = FacialLandmarksEntity(
            sessionId = sessionId,
            landmarksData = landmarksArray.toString(),
            meshTopology = topologyArray.toString()
        )
        database.facialLandmarksDao().insert(entity)
    }

    suspend fun saveSkinRegions(sessionId: String, regions: List<RegionPolygon>) = withContext(Dispatchers.IO) {
        val entities = regions.map { region ->
            val polyCoords = JSONArray()
            region.points.forEach { pt ->
                polyCoords.put(JSONObject().apply {
                    put("x", pt.x)
                    put("y", pt.y)
                })
            }

            val analysisResults = JSONObject().apply {
                put("score", region.score)
                put("hydration", region.hydration)
                put("oiliness", region.oiliness)
                put("texture", region.texture)
                put("color", region.colorHex)
                put("issues", JSONArray(region.issues))
            }

            SkinRegionEntity(
                sessionId = sessionId,
                regionName = region.id,
                polygonCoordinates = polyCoords.toString(),
                aiAnalysisResults = analysisResults.toString()
            )
        }
        database.skinRegionDao().insertAll(entities)
    }

    suspend fun saveEnvironmentalMetrics(
        sessionId: String,
        yaw: Float,
        pitch: Float,
        roll: Float,
        distance: Float,
        lighting: Float,
        sharpness: Float
    ) = withContext(Dispatchers.IO) {
        val metrics = EnvironmentalMetricsEntity(
            sessionId = sessionId,
            poseYaw = yaw,
            posePitch = pitch,
            poseRoll = roll,
            distanceScore = distance,
            lightingScore = lighting,
            sharpnessScore = sharpness,
            conditionsPassed = (distance >= 0.85f && lighting >= 0.85f && sharpness >= 0.85f)
        )
        database.environmentalMetricsDao().insert(metrics)
    }

    suspend fun saveFinalReport(
        sessionId: String,
        overallScore: Float,
        estimatedSkinAge: Int,
        recommendationsJson: String,
        fullReportJson: String
    ): FinalAnalysisReportEntity = withContext(Dispatchers.IO) {
        val report = FinalAnalysisReportEntity(
            sessionId = sessionId,
            overallScore = overallScore,
            estimatedSkinAge = estimatedSkinAge,
            recommendations = recommendationsJson,
            fullReportJson = fullReportJson,
            generatedAt = System.currentTimeMillis()
        )
        database.finalAnalysisReportDao().insert(report)
        updateSessionStatus(sessionId, SessionStatusEnum.COMPLETED)
        report
    }

    suspend fun getReportForSession(sessionId: String): FinalAnalysisReportEntity? = withContext(Dispatchers.IO) {
        database.finalAnalysisReportDao().getBySessionId(sessionId)
    }

    suspend fun deleteSession(sessionId: String) = withContext(Dispatchers.IO) {
        database.analysisSessionDao().deleteSession(sessionId)
    }
}
