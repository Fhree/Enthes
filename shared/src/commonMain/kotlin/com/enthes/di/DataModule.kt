package com.enthes.di

import app.cash.sqldelight.db.SqlDriver
import com.enthes.data.repository.SqlDelightDiseaseActivityRepository
import com.enthes.database.DatabaseDriverFactory
import com.enthes.database.EnthesDatabase
import com.enthes.domain.repository.DiseaseActivityRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single {
        val driver = getOrNull<DatabaseDriverFactory>()?.createDriver() ?: get<SqlDriver>()
        EnthesDatabase(driver)
    }
    single { SqlDelightDiseaseActivityRepository(get<EnthesDatabase>()) } bind DiseaseActivityRepository::class
}
