package com.example.opportunitytracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY createdAt DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity)

    @Delete
    suspend fun deleteApplication(application: ApplicationEntity)

    @Query("SELECT COUNT(*) FROM applications")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM applications WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>
}
