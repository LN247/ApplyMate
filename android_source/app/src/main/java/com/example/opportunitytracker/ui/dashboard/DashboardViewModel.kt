package com.example.opportunitytracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.opportunitytracker.data.ApplicationEntity
import com.example.opportunitytracker.data.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val applications: List<ApplicationEntity> = emptyList(),
    val totalCount: Int = 0,
    val pendingCount: Int = 0,
    val acceptedCount: Int = 0,
    val rejectedCount: Int = 0
)

class DashboardViewModel(private val repository: Repository) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.allApplications,
        repository.getTotalCount(),
        repository.getCountByStatus("Pending"),
        repository.getCountByStatus("Accepted"),
        repository.getCountByStatus("Rejected")
    ) { apps, total, pending, accepted, rejected ->
        DashboardUiState(apps, total, pending, accepted, rejected)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun deleteApplication(application: ApplicationEntity) {
        viewModelScope.launch {
            repository.delete(application)
        }
    }
}
