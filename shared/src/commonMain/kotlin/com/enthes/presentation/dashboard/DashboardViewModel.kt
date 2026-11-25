package com.enthes.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enthes.domain.repository.DiseaseActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DiseaseActivityRepository,
) : ViewModel() {

    private val _error = MutableStateFlow<DashboardError?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllActivities(),
        _error,
    ) { activities, error ->
        if (activities.isEmpty() && (error == null)) {
            DashboardUiState.Empty
        } else {
            DashboardUiState.Success(
                activities = activities,
                error = error,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading,
    )

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.OnDeleteActivity -> {
                viewModelScope.launch {
                    repository.deleteActivity(event.id)
                        .onSuccess {
                            _error.value = null
                        }
                        .onFailure {
                            _error.value = DashboardError.DeleteFailed
                        }
                }
            }
            DashboardEvent.OnDismissError -> {
                _error.value = null
            }
        }
    }
}
