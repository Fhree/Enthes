package com.enthes.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.inMemoryDriver

actual fun createInMemorySqlDriver(): SqlDriver {
    return inMemoryDriver(EnthesDatabase.Schema)
}
