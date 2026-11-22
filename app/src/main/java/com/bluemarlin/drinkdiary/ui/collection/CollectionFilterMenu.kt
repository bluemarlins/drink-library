package com.bluemarlin.drinkdiary.ui.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.domain.model.CollectionStatus
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import io.github.bluemarlins.aura.component.AuraBadge
import io.github.bluemarlins.aura.component.AuraBadgeSize
import io.github.bluemarlins.aura.component.AuraBadgeVariant
import io.github.bluemarlins.aura.component.AuraBadgedBox
import io.github.bluemarlins.aura.component.AuraDropdownMenu
import io.github.bluemarlins.aura.component.AuraIcon
import io.github.bluemarlins.aura.component.AuraIconButton
import io.github.bluemarlins.aura.component.AuraIconButtonVariant
import io.github.bluemarlins.aura.component.AuraMenuItem
import io.github.bluemarlins.aura.component.AuraMenuSection

// 일지 필터는 앱바에 둔다. 목록 맨 위에 두면 스크롤하는 순간 기준을 바꿀 수 없다.
@Composable
fun CollectionFilterAction(
    state: CollectionUiState,
    onTypeChange: (DrinkType?) -> Unit,
    onStatusChange: (CollectionStatus?) -> Unit,
    onSortByRatingChange: (Boolean) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val active = collectionFilterSummary(state) != null
    Box {
        AuraIconButton(
            onClick = { expanded = true },
            contentDescription = if (active) "필터, 걸려 있음" else "필터",
            variant = AuraIconButtonVariant.Ghost,
            icon = {
                // 걸린 필터가 있으면 점을 찍는다. 무엇이 걸렸는지는 제목 아래 부제가 말한다.
                AuraBadgedBox(
                    badge = {
                        if (active) AuraBadge(variant = AuraBadgeVariant.Brand, size = AuraBadgeSize.Dot)
                    },
                ) {
                    AuraIcon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_filter),
                        contentDescription = null,
                    )
                }
            },
        )
        AuraDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            sections = collectionFilterSections(state),
            onItemClick = { item -> applyFilterItem(item.id, onTypeChange, onStatusChange, onSortByRatingChange) },
        )
    }
}

internal fun collectionFilterSections(state: CollectionUiState): List<AuraMenuSection> =
    listOf(
        AuraMenuSection(
            title = "주종",
            items =
                listOf(
                    AuraMenuItem(TYPE_ALL, "전체", selected = state.filter == null),
                    AuraMenuItem(
                        TYPE_WINE,
                        DrinkLabels.drinkType(DrinkType.Wine),
                        selected =
                            state.filter == DrinkType.Wine,
                    ),
                    AuraMenuItem(
                        TYPE_WHISKEY,
                        DrinkLabels.drinkType(DrinkType.Whiskey),
                        selected = state.filter == DrinkType.Whiskey,
                    ),
                ),
        ),
        AuraMenuSection(
            title = "다시 살까",
            items =
                listOf(
                    AuraMenuItem(STATUS_ALL, "전체", selected = state.statusFilter == null),
                    AuraMenuItem(
                        STATUS_REPURCHASE,
                        DrinkLabels.collectionStatus(CollectionStatus.Repurchase),
                        selected = state.statusFilter == CollectionStatus.Repurchase,
                    ),
                    AuraMenuItem(
                        STATUS_NOT_FOR_ME,
                        DrinkLabels.collectionStatus(CollectionStatus.NotForMe),
                        selected = state.statusFilter == CollectionStatus.NotForMe,
                    ),
                ),
        ),
        AuraMenuSection(
            title = "정렬",
            items =
                listOf(
                    AuraMenuItem(SORT_RECENT, "최근 순", selected = !state.sortByRating),
                    AuraMenuItem(SORT_RATING, "별점 높은 순", selected = state.sortByRating),
                ),
        ),
    )

// 앱바 부제. 기본값(전체 · 전체 · 최근 순)과 다른 것만 말한다. 아무것도 안 걸렸으면 부제가 없다.
internal fun collectionFilterSummary(state: CollectionUiState): String? =
    listOfNotNull(
        state.filter?.let(DrinkLabels::drinkType),
        state.statusFilter?.let(DrinkLabels::collectionStatus),
        if (state.sortByRating) "별점 높은 순" else null,
    ).takeIf { it.isNotEmpty() }?.joinToString(" · ")

internal fun applyFilterItem(
    id: String,
    onTypeChange: (DrinkType?) -> Unit,
    onStatusChange: (CollectionStatus?) -> Unit,
    onSortByRatingChange: (Boolean) -> Unit,
) {
    when (id) {
        TYPE_ALL -> onTypeChange(null)
        TYPE_WINE -> onTypeChange(DrinkType.Wine)
        TYPE_WHISKEY -> onTypeChange(DrinkType.Whiskey)
        STATUS_ALL -> onStatusChange(null)
        STATUS_REPURCHASE -> onStatusChange(CollectionStatus.Repurchase)
        STATUS_NOT_FOR_ME -> onStatusChange(CollectionStatus.NotForMe)
        SORT_RECENT -> onSortByRatingChange(false)
        SORT_RATING -> onSortByRatingChange(true)
    }
}

private const val TYPE_ALL = "type:all"
private const val TYPE_WINE = "type:wine"
private const val TYPE_WHISKEY = "type:whiskey"
private const val STATUS_ALL = "status:all"
private const val STATUS_REPURCHASE = "status:repurchase"
private const val STATUS_NOT_FOR_ME = "status:not-for-me"
private const val SORT_RECENT = "sort:recent"
private const val SORT_RATING = "sort:rating"
