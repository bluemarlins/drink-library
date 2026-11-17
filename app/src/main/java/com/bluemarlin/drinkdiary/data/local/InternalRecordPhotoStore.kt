package com.bluemarlin.drinkdiary.data.local

import android.content.Context
import android.net.Uri
import com.bluemarlin.drinkdiary.domain.model.AppError
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.repository.RecordPhotoStore
import com.bluemarlin.drinkdiary.domain.repository.StoredPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private const val PHOTO_DIR = "record_photos"

class InternalRecordPhotoStore(
    private val context: Context,
) : RecordPhotoStore {
    private val directory: File get() = File(context.filesDir, PHOTO_DIR)

    override suspend fun import(sourceUri: String): AppResult<String> =
        withContext(Dispatchers.IO) {
            val target = File(directory.apply { mkdirs() }, "${UUID.randomUUID()}.img")
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(sourceUri))?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                } ?: error("cannot open $sourceUri")
                Uri.fromFile(target).toString()
            }.fold(
                onSuccess = { AppResult.Success(it) },
                onFailure = {
                    target.delete()
                    AppResult.Failure(AppError.Storage)
                },
            )
        }

    override suspend fun listStored(): List<StoredPhoto> =
        withContext(Dispatchers.IO) {
            directory.listFiles().orEmpty().filter { it.isFile }.map {
                StoredPhoto(uri = Uri.fromFile(it).toString(), modifiedAtMillis = it.lastModified())
            }
        }

    override suspend fun delete(uri: String) {
        withContext(Dispatchers.IO) {
            val file = Uri.parse(uri).path?.let(::File) ?: return@withContext
            // 이 저장소가 만든 파일만 지운다. 사진 선택기 주소나 에셋 주소는 건드리지 않는다.
            if (file.parentFile?.canonicalPath == directory.canonicalPath) file.delete()
        }
    }
}
