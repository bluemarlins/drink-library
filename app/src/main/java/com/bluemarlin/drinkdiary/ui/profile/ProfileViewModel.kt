package com.bluemarlin.drinkdiary.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bluemarlin.drinkdiary.domain.model.BuyRecommendationBundle
import com.bluemarlin.drinkdiary.domain.model.DashboardOverview
import com.bluemarlin.drinkdiary.domain.model.LogHighlights
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TagPreference
import com.bluemarlin.drinkdiary.domain.model.TasteProfile
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.domain.usecase.ObserveBuyRecommendationsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveDashboardOverviewUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveLogHighlightsUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTagPreferenceUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ObserveTasteProfileUseCase
import com.bluemarlin.drinkdiary.domain.usecase.ResolveProfileReadinessUseCase
import com.bluemarlin.drinkdiary.domain.usecase.TasteThresholds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    // 첫 집계가 오기 전에는 빈 상태("이번 달 · 1월 / 0잔")가 아니라 로딩 자리표시를 그린다.
    val loaded: Boolean = false,
    val scope: TypeScope = TypeScope.Wine,
    val overview: DashboardOverview = DashboardOverview(),
    // 주종을 가리지 않는 기록 전체의 사실(이번 달·최근·자주). scope를 바꿔도 그대로다.
    val highlights: LogHighlights = LogHighlights(),
    val profile: TasteProfile? = null,
    val tagPreferences: List<TagPreference> = emptyList(),
    val readiness: ProfileReadiness =
        ProfileReadiness.NotReady(recordsNeeded = TasteThresholds.MIN_SAMPLES),
    val recommendations: BuyRecommendationBundle = BuyRecommendationBundle(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val observeOverview: ObserveDashboardOverviewUseCase,
    private val observeHighlights: ObserveLogHighlightsUseCase,
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
                    observeHighlights(),
                    observeTasteProfile(selected),
                    observeTagPreference(selected),
                    observeRecommendations(selected),
                ) { overview, highlights, profile, tags, recommendations ->
                    ProfileUiState(
                        loaded = true,
                        scope = selected,
                        overview = overview,
                        highlights = highlights,
                        profile = profile,
                        tagPreferences = tags,
                        readiness = resolveReadiness(profile),
                        recommendations = recommendations,
                    )
                }
            }
            // 병 사전(asset JSON) 파싱과 추천 점수 계산이 메인 스레드에서 돌면 첫 화면이 몇 초 늦게 채워진다.
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    fun selectScope(newScope: TypeScope) {
        scope.value = newScope
    }

    class Factory(
        private val observeOverview: ObserveDashboardOverviewUseCase,
        private val observeHighlights: ObserveLogHighlightsUseCase,
        private val observeTasteProfile: ObserveTasteProfileUseCase,
        private val observeTagPreference: ObserveTagPreferenceUseCase,
        private val observeRecommendations: ObserveBuyRecommendationsUseCase,
        private val resolveReadiness: ResolveProfileReadinessUseCase,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProfileViewModel(
                observeOverview = observeOverview,
                observeHighlights = observeHighlights,
                observeTasteProfile = observeTasteProfile,
                observeTagPreference = observeTagPreference,
                observeRecommendations = observeRecommendations,
                resolveReadiness = resolveReadiness,
            ) as T
    }
}
