package com.bluemarlin.drinkdiary.data.local

import android.content.Context
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

private const val DEFAULT_ASSET_PATH = "local_seed/seed_records.json"
private const val DEFAULT_MARKER_FILE = ".local_seed_v1_applied"

// 개발/QA용 로컬 시드 로더. local_seed/ 자산이 없는 클린 환경에서는 조용히 건너뛴다.
class LocalSeedLoader(
    private val context: Context,
    private val repository: DrinkRecordRepository,
    private val assetPath: String = DEFAULT_ASSET_PATH,
    private val markerFileName: String = DEFAULT_MARKER_FILE,
) {
    private val mutex = Mutex()

    fun parse(): List<DrinkRecord> =
        runCatching {
            val json = context.assets.open(assetPath).use { it.readBytes().decodeToString() }
            parseJson(json)
        }.getOrElse { emptyList() }

    fun parseJson(json: String): List<DrinkRecord> = DrinkRecordJson.parseRecordArray(json)

    suspend fun seedIfNeeded() {
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val seeds = parse()
                if (seeds.isEmpty()) return@withLock

                val marker = File(context.filesDir, markerFileName)
                val existing = repository.observeRecords().first()
                if (existing.isNotEmpty() && marker.exists()) return@withLock

                val existingNames = existing.mapTo(mutableSetOf()) { it.name }
                var allSucceeded = true
                for (record in seeds) {
                    if (record.name in existingNames) continue
                    val result = repository.save(record)
                    if (result is AppResult.Success) {
                        existingNames.add(record.name)
                    } else {
                        allSucceeded = false
                    }
                }
                if (allSucceeded) {
                    runCatching {
                        marker.parentFile?.mkdirs()
                        marker.writeText("1")
                    }
                }
            }
        }
    }
}
