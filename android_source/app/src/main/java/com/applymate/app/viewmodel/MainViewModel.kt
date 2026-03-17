package com.applymate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.applymate.app.data.AppDatabase
import com.applymate.app.data.ApplicationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseOf(
        application,
        AppDatabase::class.java,
        "applymate-db"
    ).build()

    private val dao = db.applicationDao()

    val applications: Flow<List<ApplicationEntity>> = dao.getAllApplications()

    fun addApplication(title: String, organization: String, status: String, deadline: String) {
        viewModelScope.launch {
            dao.insertApplication(
                ApplicationEntity(
                    title = title,
                    organization = organization,
                    status = status,
                    deadline = deadline
                )
            )
        }
    }

    fun deleteApplication(application: ApplicationEntity) {
        viewModelScope.launch {
            dao.deleteApplication(application)
        }
    }
}
