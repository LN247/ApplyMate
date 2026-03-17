package com.example.applymate.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.applymate.models.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    
    private val _applications = MutableLiveData<List<Application>>()
    val applications: LiveData<List<Application>> = _applications

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun fetchApplications() {
        val user = auth.currentUser ?: return
        _loading.value = true
        
        db.collection("applications")
            .whereEqualTo("userId", user.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                _loading.value = false
                if (e != null) return@addSnapshotListener
                
                val list = snapshot?.toObjects(Application::class.java) ?: emptyList()
                _applications.value = list
            }
    }

    fun addApplication(title: String, org: String, status: String, deadline: String) {
        val user = auth.currentUser ?: return
        val id = db.collection("applications").document().id
        val app = Application(id, user.uid, title, org, status, deadline)
        
        db.collection("applications").document(id).set(app)
    }

    fun deleteApplication(id: String) {
        db.collection("applications").document(id).delete()
    }
}
