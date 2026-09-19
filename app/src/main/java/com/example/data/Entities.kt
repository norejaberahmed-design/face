package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

enum class SessionStatusEnum(val value: String) {
    INITIALIZING("initializing"),
    DETECTING_LANDMARKS("detecting_landmarks"),
    BUILDING_MESH("building_mesh"),
    EXCLUDING_REGIONS("excluding_regions"),
    MAPPING_SKIN("mapping_skin"),
    CHECKING_CONDITIONS("checking_conditions"),
    READY("ready"),
    ANALYZING("analyzing"),
    COMPLETED("completed"),
    FAILED("failed")
}

enum class CaptureTypeEnum(val value: String) {
    RAW_IMAGE("raw_image"),
    PROCESSED_MESH("processed_mesh"),
    PROCESSED_REGIONS("processed_regions")
}

@Entity(
    tableName = "analysis_sessions",
    indices = [Index(value = ["user_id"])]
)
data class AnalysisSessionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "user_id")
    val userId: String = "user_default_beauty",
    val status: String = SessionStatusEnum.INITIALIZING.value,
    @ColumnInfo(name = "ai_model_version")
    val aiModelVersion: String = "FaceMesh-v2.4-SkinNet",
    @ColumnInfo(name = "device_info")
    val deviceInfo: String = """{"platform":"Android","camera":"Front HD","lens":"Wide"}""",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null
)

@Entity(
    tableName = "session_captures",
    foreignKeys = [
        ForeignKey(
            entity = AnalysisSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_id"])]
)
data class SessionCaptureEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
    @ColumnInfo(name = "capture_type")
    val captureType: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "facial_landmarks",
    foreignKeys = [
        ForeignKey(
            entity = AnalysisSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_id"], unique = true)]
)
data class FacialLandmarksEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "landmarks_data")
    val landmarksData: String,
    @ColumnInfo(name = "mesh_topology")
    val meshTopology: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "skin_regions",
    foreignKeys = [
        ForeignKey(
            entity = AnalysisSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["region_name"])
    ]
)
data class SkinRegionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "region_name")
    val regionName: String, // 'forehead', 'left_cheek', 'right_cheek', 'chin', 't_zone'
    @ColumnInfo(name = "polygon_coordinates")
    val polygonCoordinates: String,
    @ColumnInfo(name = "ai_analysis_results")
    val aiAnalysisResults: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "environmental_metrics",
    foreignKeys = [
        ForeignKey(
            entity = AnalysisSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_id"], unique = true)]
)
data class EnvironmentalMetricsEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "pose_yaw")
    val poseYaw: Float = 0.5f,
    @ColumnInfo(name = "pose_pitch")
    val posePitch: Float = -0.8f,
    @ColumnInfo(name = "pose_roll")
    val poseRoll: Float = 0.2f,
    @ColumnInfo(name = "distance_score")
    val distanceScore: Float = 0.94f,
    @ColumnInfo(name = "lighting_score")
    val lightingScore: Float = 0.92f,
    @ColumnInfo(name = "sharpness_score")
    val sharpnessScore: Float = 0.96f,
    @ColumnInfo(name = "conditions_passed")
    val conditionsPassed: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "final_analysis_reports",
    foreignKeys = [
        ForeignKey(
            entity = AnalysisSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["session_id"], unique = true)]
)
data class FinalAnalysisReportEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "session_id")
    val sessionId: String,
    @ColumnInfo(name = "overall_score")
    val overallScore: Float,
    @ColumnInfo(name = "estimated_skin_age")
    val estimatedSkinAge: Int,
    @ColumnInfo(name = "recommendations")
    val recommendations: String,
    @ColumnInfo(name = "full_report_json")
    val fullReportJson: String,
    @ColumnInfo(name = "generated_at")
    val generatedAt: Long = System.currentTimeMillis()
)
