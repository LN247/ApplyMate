package com.applymate.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val organization: String,
    val status: String,
    val deadline: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY createdAt DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity)

    @Delete
    suspend fun deleteApplication(application: ApplicationEntity)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY timestamp DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)
}

@Dao
interface PreferenceDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    fun getPreferenceProfile(): Flow<PreferenceProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePreferenceProfile(profile: PreferenceProfile)
}

@Dao
interface DiscoveryDao {
    @Query("SELECT * FROM discovered_opportunities ORDER BY matchScore DESC")
    fun getAllDiscovered(): Flow<List<DiscoveredOpportunity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunities(opportunities: List<DiscoveredOpportunity>)

    @Update
    suspend fun updateOpportunity(opportunity: DiscoveredOpportunity)

    @Query("DELETE FROM discovered_opportunities WHERE isSaved = 0")
    suspend fun clearUnsaved()
}

@Database(entities = [ApplicationEntity::class, DocumentEntity::class, PreferenceProfile::class, DiscoveredOpportunity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun applicationDao(): ApplicationDao
    abstract fun documentDao(): DocumentDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun discoveryDao(): DiscoveryDao
}
