package com.enthes.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.enthes.data.mapper.insertDiseaseActivity
import com.enthes.data.mapper.toDomain
import com.enthes.database.DatabaseDriverFactory
import com.enthes.database.EnthesDatabase
import com.enthes.domain.model.DiseaseActivity
import com.enthes.domain.repository.DiseaseActivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightDiseaseActivityRepository(
    database: EnthesDatabase,
) : DiseaseActivityRepository {

    constructor(driverFactory: DatabaseDriverFactory) : this(EnthesDatabase(driverFactory.createDriver()))

    private val queries = database.enthesQueries

    override fun getAllActivities(): Flow<List<DiseaseActivity>> {
        return queries.selectAllDiseaseActivities()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertActivity(activity: DiseaseActivity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            queries.insertDiseaseActivity(activity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
