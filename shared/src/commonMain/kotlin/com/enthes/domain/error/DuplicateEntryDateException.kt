package com.enthes.domain.error

import kotlinx.datetime.LocalDate

class DuplicateEntryDateException(
    val entryDate: LocalDate,
    cause: Throwable? = null,
) : Exception("An entry already exists for date $entryDate.", cause)
