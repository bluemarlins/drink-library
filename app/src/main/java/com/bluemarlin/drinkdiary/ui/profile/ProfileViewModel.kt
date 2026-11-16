package com.bluemarlin.drinkdiary.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.BuyRecommendationBundle
import com.bluemarlin.drinkdiary.domain.model.DashboardOverview
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.usecase.ObserveBuyRecommendationsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDashboardOverviewUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTagPreferenceUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTasteProfileUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ResolveProfileReadinessUseCase
import com.bluemarlin.drinkdiary.domain.usecase.TasteThresholds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val scope: TypeScope = TypeScope.Wine,
    val overview: DashboardOverview = DashboardOverview(),
    val profile: TasteProfile? = null,
    val tagPreferences: List<TagPreference> = emptyList(),
    val readiness: ProfileReadiness =
        ProfileReadiness.NotReady(recordsNeeded = TasteThresholds.MIN_SAMPLES),
    val recommendations: BuyRecommendationBundle = BuyRecommendationBundle(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val observeOverview: ObserveDashboardOverviewUseCase,
    private val observeTasteProfile: ObserveTasteProfileUseCase,
    private val observeTagPreference: ObserveTagPreferenceUseCase,
    private val observeRecommendations: ObserveBuyRecommendationsUseCase,
    private val resolveReadiness: ResolveProfileReadinessUseCase,
) : ViewModel() {
    private val scope = MutableStateFlow(TypeScope.Wine)

    val uiState: StateFlow<ProfileUiState> =
        scope
            .flatMapLatest { selected ->
                combine(
                    observeOverview(selected),
                    observeTasteProfile(selected),
                    observeTagPreference(selected),
                    observeRecommendations(selected),
                ) { overview, profile, tags, recommendations ->
                    ProfileUiState(
                        scope = selected,
                        overview = overview,
                        profile = profile,
                        tagPreferences = tags,
                        readiness = resolveReadiness(profile),
                        recommendations = recommendations,
                    )
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun selectScope(newScope: TypeScope) {
        scope.value = newScope
    }

    class Factory(
        private val observeOverview: ObserveDashboardOverviewUseCase,
        private val observeTasteProfile: ObserveTasteProfileUseCase,
        private val observeTagPreference: ObserveTagPreferenceUseCase,
        private val observeRecommendations: ObserveBuyRecommendationsUseCase,
        private val resolveReadiness: ResolveProfileReadinessUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfileViewModel(
                observeOverview = observeOverview,
                observeTasteProfile = observeTasteProfile,
                observeTagPreference = observeTagPreference,
                observeRecommendations = observeRecommendations,
                resolveReadiness = resolveReadiness,
            ) as T
    }
}
