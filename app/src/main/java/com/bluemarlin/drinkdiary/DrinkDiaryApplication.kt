package com.bluemarlin.drinkdiary

import android.app.Application
import androidx.room.Room
import com.bluemarlin.drinkdiary.data.local.AssetBottleDictionary
import com.bluemarlin.drinkdiary.data.local.DrinkDiaryDatabase
import com.bluemarlin.drinkdiary.data.local.InternalRecordPhotoStore
import com.bluemarlin.drinkdiary.data.local.LocalSeedLoader
import com.bluemarlin.drinkdiary.data.local.MIGRATION_2_3
import com.bluemarlin.drinkdiary.data.local.OrgJsonBackupCodec
import com.bluemarlin.drinkdiary.data.repository.DrinkRecordRepositoryImpl
import com.bluemarlin.drinkdiary.data.repository.UserPreferencesRepositoryImpl
import com.bluemarlin.drinkdiary.domain.repository.BottleDictionary
import com.bluemarlin.drinkdiary.domain.repository.BottleMatcher
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordBackupCodec
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.RecordPhotoStore
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import com.bluemarlin.drinkdiary.domain.usecase.BackupDrinkRecordsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveBuyRecommendationsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDashboardOverviewUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveLogHighlightsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTagPreferenceUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTasteProfileUseCase
import com.bluemarlin.drinkdiary.domain.usecase.PruneRecordPhotosUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ResolveProfileReadinessUseCase

class DrinkDiaryApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}

class AppContainer(
    application: Application,
) {
    private val database: DrinkDiaryDatabase =
        Room
            .databaseBuilder(application, DrinkDiaryDatabase::class.java, "taste_archive.db")
            .addMigrations(MIGRATION_2_3)
            .build()

    val drinkRecordRepository: DrinkRecordRepository =
        DrinkRecordRepositoryImpl(database.drinkRecordDao())

    val recordPhotoStore: RecordPhotoStore = InternalRecordPhotoStore(application)
    val pruneRecordPhotosUseCase = PruneRecordPhotosUseCase(drinkRecordRepository, recordPhotoStore)

    val userPreferencesRepository: UserPreferencesRepository =
        UserPreferencesRepositoryImpl(application)

    val observeTasteProfileUseCase = ObserveTasteProfileUseCase(drinkRecordRepository)

    private val bottleAssets = AssetBottleDictionary(application)
    val bottleDictionary: BottleDictionary = BottleMatcher { bottleAssets.entries }

    val observeTagPreferenceUseCase = ObserveTagPreferenceUseCase(drinkRecordRepository, bottleDictionary)
    val resolveProfileReadinessUseCase = ResolveProfileReadinessUseCase()
    val observeDashboardOverviewUseCase = ObserveDashboardOverviewUseCase(drinkRecordRepository)
    val observeLogHighlightsUseCase = ObserveLogHighlightsUseCase(drinkRecordRepository)
    val observeBuyRecommendationsUseCase =
        ObserveBuyRecommendationsUseCase(
            repository = drinkRecordRepository,
            dictionary = bottleDictionary,
            observeTasteProfile = observeTasteProfileUseCase,
            observeTagPreference = observeTagPreferenceUseCase,
            resolveReadiness = resolveProfileReadinessUseCase,
        )

    private val backupCodec: DrinkRecordBackupCodec = OrgJsonBackupCodec()
    val backupDrinkRecordsUseCase = BackupDrinkRecordsUseCase(drinkRecordRepository, backupCodec)
    val localSeedLoader = LocalSeedLoader(application, drinkRecordRepository)
}
