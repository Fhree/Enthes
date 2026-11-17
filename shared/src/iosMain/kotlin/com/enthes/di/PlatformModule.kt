package com.enthes.di

import com.enthes.database.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single { DatabaseDriverFactory() }
}
