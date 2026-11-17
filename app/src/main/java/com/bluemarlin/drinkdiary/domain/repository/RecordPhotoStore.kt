package com.bluemarlin.drinkdiary.domain.repository

import com.bluemarlin.drinkdiary.domain.model.AppResult

data class StoredPhoto(
    val uri: String,
    val modifiedAtMillis: Long,
)

/**
 * 기록 사진을 앱이 소유한 저장소에 보관한다.
 * 사진 선택기가 주는 주소는 앱 프로세스가 끝나면 읽을 수 없게 되므로, 고른 즉시 복사해 둔다.
 */
interface RecordPhotoStore {
    /** [sourceUri]를 복사하고, 기록에 저장할 새 주소를 돌려준다. */
    suspend fun import(sourceUri: String): AppResult<String>

    suspend fun listStored(): List<StoredPhoto>

    suspend fun delete(uri: String)
}
