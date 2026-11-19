package com.bluemarlin.drinkdiary.ui.settings

import android.os.Looper
import com.bluemarlin.drinkdiary.domain.model.AppResult
import com.bluemarlin.drinkdiary.domain.model.AppearanceSettings
import com.bluemarlin.drinkdiary.domain.model.ColorModePreference
import com.bluemarlin.drinkdiary.domain.model.DrinkRecord
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.SurfaceStyle
import com.bluemarlin.drinkdiary.domain.model.TagCategory
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordBackupCodec
import com.bluemarlin.drinkdiary.domain.repository.DrinkRecordRepository
import com.bluemarlin.drinkdiary.domain.repository.UserPreferencesRepository
import com.bluemarlin.drinkdiary.domain.usecase.BackupDrinkRecordsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDashboardOverviewUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SettingsViewModelAppearanceTest {
    private class FakeRecords : DrinkRecordRepository {
        override fun observeRecords(type: DrinkType?): Flow<List<DrinkRecord>> = flowOf(emptyList())

        override fun observeRecord(id: Long): Flow<DrinkRecord?> = flowOf(null)

        override fun observeSearchResults(query: String): Flow<List<DrinkRecord>> = flowOf(emptyList())

        override suspend fun save(record: DrinkRecord): AppResult<Long> = AppResult.Success(1L)

        override suspend fun deleteById(id: Long): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeCodec : DrinkRecordBackupCodec {
        override fun encode(records: List<DrinkRecord>): String = "[]"

        override fun decode(json: String): AppResult<List<DrinkRecord>> = AppResult.Success(emptyList())
    }

    private class FakePreferences : UserPreferencesRepository {
        val stored = MutableStateFlow(AppearanceSettings())

        override val isProUser: Flow<Boolean> = flowOf(false)

        override suspend fun setProUser(isPro: Boolean) = Unit

        override val alwaysAskTags: Flow<Set<TagCategory>> = flowOf(emptySet())

        override val hasChosenTagPreferences: Flow<Boolean> = flowOf(false)

        override suspend fun setAlwaysAskTags(tags: Set<TagCategory>) = Unit

        override val appearance: Flow<AppearanceSettings> = stored

        override suspend fun setSurfaceStyle(style: SurfaceStyle) {
            stored.value = stored.value.copy(surfaceStyle = style)
        }

        override suspend fun setColorMode(mode: ColorModePreference) {
            stored.value = stored.value.copy(colorMode = mode)
        }
    }

    @Test
    fun `choosing a material and color mode is saved and reflected in the state`() {
        val records = FakeRecords()
        val preferences = FakePreferences()
        val viewModel =
            SettingsViewModel(
                observeOverview = ObserveDashboardOverviewUseCase(records),
                backupUseCase = BackupDrinkRecordsUseCase(records, FakeCodec()),
                preferences = preferences,
            )
        val collector = CoroutineScope(Dispatchers.Main).launch { viewModel.uiState.collect {} }
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(AppearanceSettings(), viewModel.uiState.value.appearance)

        viewModel.selectSurfaceStyle(SurfaceStyle.GlassMorphism)
        viewModel.selectColorMode(ColorModePreference.Dark)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(SurfaceStyle.GlassMorphism, preferences.stored.value.surfaceStyle)
        assertEquals(ColorModePreference.Dark, preferences.stored.value.colorMode)
        assertEquals(
            AppearanceSettings(SurfaceStyle.GlassMorphism, ColorModePreference.Dark),
            viewModel.uiState.value.appearance,
        )
        collector.cancel()
    }
}
