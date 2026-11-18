package com.enthes.data.repository

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
    var failureException: Throwable = IllegalStateException("Failed to insert activity")

    override fun getAllActivities(): Flow<List<DiseaseActivity>> = activitiesFlow.asStateFlow()

    override suspend fun insertActivity(activity: DiseaseActivity): Result<Unit> {
        if (shouldFailOnInsert) {
            return Result.failure(failureException)
        }
        if (existingActivities.any { it.entryDate == activity.entryDate }) {
            return Result.failure(IllegalArgumentException("Duplicate entryDate: ${activity.entryDate}"))
        }
        existingActivities.add(activity)
        activitiesFlow.value = existingActivities.toList()
        return Result.success(Unit)
    }

    fun emitActivities(activities: List<DiseaseActivity>) {
        existingActivities.clear()
        existingActivities.addAll(activities)
        activitiesFlow.value = activities
    }
}
