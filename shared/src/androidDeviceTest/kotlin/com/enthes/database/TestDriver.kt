package com.enthes.database

import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual fun createInMemorySqlDriver(): SqlDriver {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return AndroidSqliteDriver(EnthesDatabase.Schema, context, null)
}