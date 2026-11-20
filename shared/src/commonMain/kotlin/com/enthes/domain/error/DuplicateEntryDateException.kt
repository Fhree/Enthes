package com.enthes.domain.error

class DuplicateEntryDateException(
    val entryDate: String,
    cause: Throwable? = null,
) : Exception("An entry already exists for date $entryDate.", cause)
