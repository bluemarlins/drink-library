package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.RestoreSummary
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordBackupCodec
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.flow.first

class BackupDrinkRecordsUseCase(
    private val repository: DrinkRecordRepository,
    private val codec: DrinkRecordBackupCodec,
) {
    suspend fun exportJson(): AppResult<String> =
        runCatching {
            val records = repository.observeRecords(null).first()
            codec.encode(records)
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Failure(AppError.Storage) },
        )

    suspend fun restoreFromJson(json: String): AppResult<RestoreSummary> {
        val parsed =
            when (val decoded = codec.decode(json)) {
                is AppResult.Success -> decoded.value
                is AppResult.Failure -> return decoded
            }

        val existingKeys = repository.observeRecords(null).first().mapTo(mutableSetOf()) { it.dedupKey() }
        var imported = 0
        var skipped = 0
        for (record in parsed) {
            val key = record.dedupKey()
            if (!existingKeys.add(key)) {
                skipped++
                continue
            }
            when (val saved = repository.save(record.copy(id = 0L))) {
                is AppResult.Success -> imported++
                is AppResult.Failure -> return saved
            }
        }
        return AppResult.Success(RestoreSummary(importedCount = imported, skippedCount = skipped))
    }

    private fun DrinkRecord.dedupKey(): String = "${type.name}|${name.trim().lowercase()}|$recordedAtMillis"
}
