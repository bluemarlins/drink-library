package com.bluemarlin.drinkdiary.data.local

import android.content.Context
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
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

    fun parseJson(json: String): List<DrinkRecord> =
        runCatching {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { index ->
                parseRecord(array.getJSONObject(index))
            }
        }.getOrElse { emptyList() }

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

    private fun parseRecord(row: JSONObject): DrinkRecord? {
        val type = enumOrNull<DrinkType>(row.optStringOrNull("type")) ?: return null
        val name = row.optStringOrNull("name") ?: return null
        val rating = row.optDouble("rating").takeIf { !it.isNaN() && it > 0.0 } ?: return null
        val collectionStatus =
            enumOrNull<CollectionStatus>(row.optStringOrNull("collectionStatus"))
                ?: CollectionStatus.Normal
        val vintage = if (row.isNull("vintage")) null else row.optInt("vintage").takeIf { it > 0 }
        val servingStyle = enumOrNull<ServingStyle>(row.optStringOrNull("servingStyle"))
        val price = if (row.isNull("price")) null else row.optLong("price").takeIf { it > 0L }
        val recordedAtMillis =
            if (row.isNull("recordedAtMillis")) {
                System.currentTimeMillis()
            } else {
                row.optLong("recordedAtMillis", System.currentTimeMillis())
            }

        return DrinkRecord(
            type = type,
            name = name,
            vintage = vintage,
            servingStyle = servingStyle,
            taste = parseTaste(row.optJSONObject("taste")),
            tags = parseTags(row.optJSONObject("tags")),
            rating = rating,
            collectionStatus = collectionStatus,
            imageUri = row.optStringOrNull("imageUri"),
            price = price,
            place = row.optStringOrNull("place"),
            memo = row.optStringOrNull("memo"),
            recordedAtMillis = recordedAtMillis,
        )
    }

    private fun parseTaste(tasteObj: JSONObject?): TasteInput {
        if (tasteObj == null) return TasteInput()
        val answers = mutableMapOf<Trait, TraitAnswer>()
        for (key in tasteObj.keys()) {
            val trait = enumOrNull<Trait>(key) ?: continue
            val answer = enumOrNull<TraitAnswer>(tasteObj.optStringOrNull(key)) ?: continue
            answers[trait] = answer
        }
        return TasteInput(answers)
    }

    private fun parseTags(tagsObj: JSONObject?): DrinkTags {
        if (tagsObj == null) return DrinkTags()
        val stored = mutableMapOf<TagCategory, String>()
        for (key in tagsObj.keys()) {
            val category = enumOrNull<TagCategory>(key) ?: continue
            val value = tagsObj.optStringOrNull(key) ?: continue
            stored[category] = value
        }
        return DrinkTags.from(stored)
    }

    private fun JSONObject.optStringOrNull(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

    private inline fun <reified T : Enum<T>> enumOrNull(raw: String?): T? =
        raw?.takeIf { it.isNotBlank() }?.let { name -> enumValues<T>().find { it.name == name } }
}
