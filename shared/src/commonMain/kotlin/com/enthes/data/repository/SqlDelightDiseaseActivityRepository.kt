package com.enthes.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.enthes.data.mapper.insertDiseaseActivity
import com.enthes.data.mapper.toDomain
import com.enthes.data.mapper.updateActivity
import com.enthes.database.EnthesDatabase
import com.enthes.domain.error.DuplicateEntryDateException
import com.enthes.domain.model.DiseaseActivity
import com.enthes.domain.repository.DiseaseActivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlin.coroutines.cancellation.CancellationException

class SqlDelightDiseaseActivityRepository(
    database: EnthesDatabase,
) : DiseaseActivityRepository {

    private val queries = database.enthesQueries

    override fun getAllActivities(): Flow<List<DiseaseActivity>> {
        return queries.selectAllDiseaseActivities()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getActivityById(id: String): DiseaseActivity? = withContext(Dispatchers.IO) {
        queries.selectById(id).executeAsOneOrNull()?.toDomain()
    }

    override suspend fun insertActivity(activity: DiseaseActivity): Result<Unit> =
        executeActivityWrite(activity.entryDate) {
            queries.insertDiseaseActivity(activity)
        }

    override suspend fun updateActivity(activity: DiseaseActivity): Result<Unit> =
        executeActivityWrite(activity.entryDate) {
            queries.updateActivity(activity)
        }

    override suspend fun deleteActivity(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        val result = runCatching {
            queries.deleteById(id)
            Unit
        }
        val failure = result.exceptionOrNull()
        if (failure is CancellationException) {
            throw failure
        } else {
            result
        }
    }

    private suspend fun executeActivityWrite(
        entryDate: LocalDate,
        block: () -> Unit,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val result = runCatching(block)
        val failure = result.exceptionOrNull()
        when {
            failure == null -> result
            failure is CancellationException -> throw failure
            failure.isUniqueConstraintViolation() -> {
                Result.failure(DuplicateEntryDateException(entryDate, failure))
            }
            else -> result
        }
    }

    private fun Throwable.isUniqueConstraintViolation(): Boolean =
        generateSequence(this) { it.cause }.any { error ->
            val message = error.message.orEmpty()
            message.contains("UNIQUE constraint failed", ignoreCase = true) ||
                message.contains("SQLITE_CONSTRAINT_UNIQUE", ignoreCase = true)
        }
}
