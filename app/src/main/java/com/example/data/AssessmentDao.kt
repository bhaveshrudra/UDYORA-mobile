package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessments WHERE userId = :userId ORDER BY date DESC")
    fun getAssessmentsForUser(userId: String): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM assessments ORDER BY date DESC")
    fun getAllAssessments(): Flow<List<AssessmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AssessmentEntity)
}
