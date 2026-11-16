package com.bluemarlin.drinkdiary.data.local

import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordBackupCodec

class OrgJsonBackupCodec(
    private val nowMillis: () -> Long = System::currentTimeMillis,
) : DrinkRecordBackupCodec {
    override fun encode(records: List<DrinkRecord>): String = DrinkRecordJson.encodeBackup(records, nowMillis())

    override fun decode(json: String): AppResult<List<DrinkRecord>> =
        DrinkRecordJson.decodeBackup(json).fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Failure(AppError.Storage) },
        )
}
