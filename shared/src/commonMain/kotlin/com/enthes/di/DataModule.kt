package com.enthes.di

import com.enthes.data.repository.SqlDelightDiseaseActivityRepository
import com.enthes.database.DatabaseDriverFactory
import com.enthes.database.EnthesDatabase
import com.enthes.domain.repository.DiseaseActivityRepository
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    single { EnthesDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { SqlDelightDiseaseActivityRepository(get<EnthesDatabase>()) } bind DiseaseActivityRepository::class
}
