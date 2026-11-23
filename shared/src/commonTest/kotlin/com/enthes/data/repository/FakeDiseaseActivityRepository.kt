package com.enthes.data.repository

import com.enthes.domain.error.DuplicateEntryDateException
import com.enthes.domain.model.DiseaseActivity
import com.enthes.domain.repository.DiseaseActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeDiseaseActivityRepository(
    initialActivities: List<DiseaseActivity> = emptyList(),
) : DiseaseActivityRepository {

    private val activitiesFlow = MutableStateFlow(initialActivities)
    private val existingActivities = initialActivities.toMutableList()

    var shouldFailOnInsert: Boolean = false
    var shouldFailOnDelete: Boolean = false
    var failureException: Throwable = IllegalStateException("Failed to insert activity")
    var deleteFailureException: Throwable = IllegalStateException("Failed to delete activity")

    override fun getAllActivities(): Flow<List<DiseaseActivity>> = activitiesFlow.asStateFlow()

    override suspend fun insertActivity(activity: DiseaseActivity): Result<Unit> = when {
        shouldFailOnInsert -> Result.failure(failureException)
        existingActivities.any { it.entryDate == activity.entryDate } -> {
            Result.failure(DuplicateEntryDateException(activity.entryDate))
        }
        else -> {
            existingActivities.add(activity)
            activitiesFlow.value = existingActivities.toList()
            Result.success(Unit)
        }
    }

    override suspend fun deleteActivity(id: String): Result<Unit> = if (shouldFailOnDelete) {
        Result.failure(deleteFailureException)
    } else {
        existingActivities.removeAll { it.id == id }
        activitiesFlow.value = existingActivities.toList()
        Result.success(Unit)
    }

    fun emitActivities(activities: List<DiseaseActivity>) {
        existingActivities.clear()
        existingActivities.addAll(activities)
        activitiesFlow.value = activities
    }
}
