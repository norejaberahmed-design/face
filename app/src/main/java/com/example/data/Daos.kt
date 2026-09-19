package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisSessionDao {
    @Query("SELECT * FROM analysis_sessions ORDER BY created_at DESC")
    fun getAllSessions(): Flow<List<AnalysisSessionEntity>>

    @Query("SELECT * FROM analysis_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): AnalysisSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AnalysisSessionEntity)

    @Update
    suspend fun updateSession(session: AnalysisSessionEntity)

    @Query("DELETE FROM analysis_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)
}

@Dao
interface FacialLandmarksDao {
    @Query("SELECT * FROM facial_landmarks WHERE session_id = :sessionId LIMIT 1")
    suspend fun getBySessionId(sessionId: String): FacialLandmarksEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(landmarks: FacialLandmarksEntity)
}

@Dao
interface SkinRegionDao {
    @Query("SELECT * FROM skin_regions WHERE session_id = :sessionId")
    fun getRegionsBySessionId(sessionId: String): Flow<List<SkinRegionEntity>>

    @Query("SELECT * FROM skin_regions WHERE session_id = :sessionId")
    suspend fun getRegionsListBySessionId(sessionId: String): List<SkinRegionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(regions: List<SkinRegionEntity>)
}

@Dao
interface EnvironmentalMetricsDao {
    @Query("SELECT * FROM environmental_metrics WHERE session_id = :sessionId LIMIT 1")
    suspend fun getBySessionId(sessionId: String): EnvironmentalMetricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metrics: EnvironmentalMetricsEntity)
}

@Dao
interface FinalAnalysisReportDao {
    @Query("SELECT * FROM final_analysis_reports ORDER BY generated_at DESC")
    fun getAllReports(): Flow<List<FinalAnalysisReportEntity>>

    @Query("SELECT * FROM final_analysis_reports WHERE session_id = :sessionId LIMIT 1")
    suspend fun getBySessionId(sessionId: String): FinalAnalysisReportEntity?

    @Query("SELECT * FROM final_analysis_reports WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FinalAnalysisReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(report: FinalAnalysisReportEntity)

    @Query("DELETE FROM final_analysis_reports WHERE id = :id")
    suspend fun deleteReport(id: String)
}
