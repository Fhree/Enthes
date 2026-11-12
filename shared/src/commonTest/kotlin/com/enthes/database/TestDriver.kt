package com.enthes.database

import app.cash.sqldelight.db.SqlDriver

expect fun createInMemorySqlDriver(): SqlDriver
