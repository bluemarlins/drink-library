package com.bluemarlin.drinkdiary.domain.usecase

import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.RecordPhotoStore
import kotlinx.coroutines.flow.first

private const val ONE_HOUR_MILLIS = 60L * 60L * 1000L

/**
 * 어떤 기록도 가리키지 않는 사진 파일을 지운다(사진 교체, 기록 삭제, 저장 없이 나간 입력).
 * 방금 고른 사진은 아직 저장 전일 수 있으므로 [graceMillis]보다 오래된 파일만 지운다.
 */
class PruneRecordPhotosUseCase(
    private val repository: DrinkRecordRepository,
    private val photoStore: RecordPhotoStore,
    private val now: () -> Long = System::currentTimeMillis,
    private val graceMillis: Long = ONE_HOUR_MILLIS,
) {
    suspend operator fun invoke(): Int {
        val referenced = repository.observeRecords(null).first().mapNotNullTo(mutableSetOf()) { it.imageUri }
        val cutoff = now() - graceMillis
        val orphans = photoStore.listStored().filter { it.uri !in referenced && it.modifiedAtMillis < cutoff }
        orphans.forEach { photoStore.delete(it.uri) }
        return orphans.size
    }
}
