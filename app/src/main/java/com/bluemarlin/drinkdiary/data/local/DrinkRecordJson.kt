package com.bluemarlin.drinkdiary.data.local

import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkTags
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ServingStyle
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.model.TasteInput
import com.bluemarlin.drinkdiary.domain.model.Trait
import com.bluemarlin.drinkdiary.domain.model.TraitAnswer
import org.json.JSONArray
import org.json.JSONObject

internal object DrinkRecordJson {
    const val BACKUP_SCHEMA_VERSION = 1

    fun parseRecordArray(json: String): List<DrinkRecord> =
        runCatching {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { index ->
                parseRecordObject(array.optJSONObject(index) ?: return@mapNotNull null)
            }
        }.getOrElse { emptyList() }

    fun encodeBackup(
        records: List<DrinkRecord>,
        exportedAtMillis: Long,
    ): String {
        val array = JSONArray()
        records.forEach { array.put(toJsonObject(it)) }
        return JSONObject()
            .put("schemaVersion", BACKUP_SCHEMA_VERSION)
            .put("exportedAtMillis", exportedAtMillis)
            .put("records", array)
            .toString(2)
    }

    fun decodeBackup(json: String): Result<List<DrinkRecord>> =
        runCatching {
            val trimmed = json.trim()
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                (0 until array.length()).mapNotNull { index ->
                    array.optJSONObject(index)?.let(::parseRecordObject)
                }
            } else {
                val root = JSONObject(trimmed)
                val array = root.getJSONArray("records")
                (0 until array.length()).mapNotNull { index ->
                    array.optJSONObject(index)?.let(::parseRecordObject)
                }
            }
        }

    fun toJsonObject(record: DrinkRecord): JSONObject {
        val tasteObj = JSONObject()
        record.taste.answers.forEach { (trait, answer) ->
            tasteObj.put(trait.name, answer.name)
        }
        val tagsObj = JSONObject()
        record.tags.entries.forEach { (category, value) ->
            tagsObj.put(category.name, value)
        }
        return JSONObject().apply {
            put("type", record.type.name)
            put("name", record.name)
            put("vintage", record.vintage ?: JSONObject.NULL)
            put("servingStyle", record.servingStyle?.name ?: JSONObject.NULL)
            put("rating", record.rating)
            put("collectionStatus", record.collectionStatus.name)
            put("imageUri", record.imageUri ?: JSONObject.NULL)
            put("price", record.price ?: JSONObject.NULL)
            put("place", record.place ?: JSONObject.NULL)
            put("memo", record.memo ?: JSONObject.NULL)
            put("recordedAtMillis", record.recordedAtMillis)
            put("taste", tasteObj)
            put("tags", tagsObj)
        }
    }

    fun parseRecordObject(row: JSONObject): DrinkRecord? {
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
