package com.example.opportunitytracker.data

import kotlinx.coroutines.flow.Flow

class Repository(private val applicationDao: ApplicationDao) {
    val allApplications: Flow<List<ApplicationEntity>> = applicationDao.getAllApplications()
    
    fun getTotalCount(): Flow<Int> = applicationDao.getTotalCount()
    fun getCountByStatus(status: String): Flow<Int> = applicationDao.getCountByStatus(status)

    suspend fun insert(application: ApplicationEntity) {
        applicationDao.insertApplication(application)
    }

    suspend fun delete(application: ApplicationEntity) {
        applicationDao.deleteApplication(application)
    }
}
