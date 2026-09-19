package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AnalysisSessionEntity::class,
        SessionCaptureEntity::class,
        FacialLandmarksEntity::class,
        SkinRegionEntity::class,
        EnvironmentalMetricsEntity::class,
        FinalAnalysisReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BeautyDatabase : RoomDatabase() {
    abstract fun analysisSessionDao(): AnalysisSessionDao
    abstract fun facialLandmarksDao(): FacialLandmarksDao
    abstract fun skinRegionDao(): SkinRegionDao
    abstract fun environmentalMetricsDao(): EnvironmentalMetricsDao
    abstract fun finalAnalysisReportDao(): FinalAnalysisReportDao

    companion object {
        @Volatile
        private var INSTANCE: BeautyDatabase? = null

        fun getInstance(context: Context): BeautyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeautyDatabase::class.java,
                    "beauty_intelligence_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
