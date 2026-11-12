package com.enthes.domain.repository

import com.enthes.domain.model.DiseaseActivity
import kotlinx.coroutines.flow.Flow

interface DiseaseActivityRepository {
    fun getAllActivities(): Flow<List<DiseaseActivity>>
    suspend fun insertActivity(activity: DiseaseActivity): Result<Unit>
}
