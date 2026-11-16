package com.bluemarlin.drinkdiary.domain.repository

import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord

interface DrinkRecordBackupCodec {
    fun encode(records: List<DrinkRecord>): String

    fun decode(json: String): AppResult<List<DrinkRecord>>
}
