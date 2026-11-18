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

    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllActivities(),
        _errorMessage,
    ) { activities, errorMessage ->
        if (activities.isEmpty() && (errorMessage == null)) {
            DashboardUiState.Empty
        } else {
            DashboardUiState.Success(
                activities = activities,
                errorMessage = errorMessage,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading,
    )

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.OnSaveCheckin -> {
                viewModelScope.launch {
                    repository.insertActivity(event.activity)
                        .onSuccess {
                            _errorMessage.value = null
                        }
                        .onFailure { error ->
                            _errorMessage.value = error.message ?: "Failed to save check-in"
                        }
                }
            }
            DashboardEvent.OnDismissError -> {
                _errorMessage.value = null
            }
        }
    }
}
