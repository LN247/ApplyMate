package com.applymate.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.applymate.app.data.*
import androidx.work.*
import com.applymate.app.worker.DiscoveryWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "applymate-db"
    ).fallbackToDestructiveMigration().build()

    private val appDao = db.applicationDao()
    private val docDao = db.documentDao()
    private val prefDao = db.preferenceDao()
    private val discoveryDao = db.discoveryDao()

    val applications: Flow<List<ApplicationEntity>> = appDao.getAllApplications()
    val documents: Flow<List<DocumentEntity>> = docDao.getAllDocuments()
    val preferenceProfile: Flow<PreferenceProfile?> = prefDao.getPreferenceProfile()
    val discoveredOpportunities: Flow<List<DiscoveredOpportunity>> = discoveryDao.getAllDiscovered()

    private val workManager = WorkManager.getInstance(application)

    init {
        scheduleDiscovery()
    }

    private fun scheduleDiscovery() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val discoveryRequest = PeriodicWorkRequestBuilder<DiscoveryWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "DiscoveryWork",
            ExistingPeriodicWorkPolicy.KEEP,
            discoveryRequest
        )
    }

    fun updatePreferences(profile: PreferenceProfile) {
        viewModelScope.launch {
            prefDao.updatePreferenceProfile(profile)
            // Trigger immediate discovery after preference update
            val immediateRequest = OneTimeWorkRequestBuilder<DiscoveryWorker>().build()
            workManager.enqueue(immediateRequest)
        }
    }

    fun saveDiscoveredOpportunity(opportunity: DiscoveredOpportunity) {
        viewModelScope.launch {
            discoveryDao.updateOpportunity(opportunity.copy(isSaved = true))
            // Also add to main applications list
            appDao.insertApplication(
                ApplicationEntity(
                    title = opportunity.title,
                    organization = opportunity.organization,
                    status = "Pending",
                    deadline = "TBD"
                )
            )
        }
    }

    fun addApplication(title: String, organization: String, status: String, deadline: String) {
        viewModelScope.launch {
            appDao.insertApplication(
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
            appDao.deleteApplication(application)
        }
    }

    fun uploadDocument(file: File, category: String, locationName: String) {
        val userId = auth.currentUser?.uid ?: "anonymous"
        val timestamp = System.currentTimeMillis()
        val dateSuffix = SimpleDateFormat("dd-MM-yyyy_HHmm", Locale.getDefault()).format(Date(timestamp))
        val fileName = "${file.nameWithoutExtension}_$dateSuffix.${file.extension}"
        val storagePath = "users/$userId/documents/$fileName"
        val storageRef = storage.reference.child(storagePath)

        viewModelScope.launch {
            try {
                // 1. Upload to Firebase Storage
                val uploadTask = storageRef.putFile(android.net.Uri.fromFile(file)).await()
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 2. Save to Firestore
                val docData = hashMapOf(
                    "fileUrl" to downloadUrl,
                    "timestamp" to timestamp,
                    "location" to locationName,
                    "category" to category,
                    "name" to fileName
                )
                firestore.collection("users").document(userId)
                    .collection("documents").add(docData).await()

                // 3. Save to Local Room DB
                docDao.insertDocument(
                    DocumentEntity(
                        name = fileName,
                        fileUrl = downloadUrl,
                        timestamp = timestamp,
                        location = locationName,
                        category = category,
                        localPath = file.absolutePath
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch {
            try {
                docDao.deleteDocument(document)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onOpportunityApplied(opportunity: DiscoveredOpportunity, location: String) {
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
            
            // Auto-Logger: Create a Pending entry with Time and GPS metadata
            appDao.insertApplication(
                ApplicationEntity(
                    title = opportunity.title,
                    organization = opportunity.organization,
                    status = "Pending",
                    deadline = "Applied on $dateStr",
                    notes = "Auto-logged from Discovery. Captured in: $location"
                )
            )
        }
    }
}
