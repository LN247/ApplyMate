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

@Database(entities = [ApplicationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun applicationDao(): ApplicationDao
}
