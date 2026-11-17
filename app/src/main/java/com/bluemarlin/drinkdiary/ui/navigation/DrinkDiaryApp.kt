package com.bluemarlin.drinkdiary.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.DrinkLabels
import com.bluemarlin.drinkdiary.ui.collection.CollectionScreen
import com.bluemarlin.drinkdiary.ui.collection.CollectionViewModel
import com.bluemarlin.drinkdiary.ui.collection.RecordDetailScreen
import com.bluemarlin.drinkdiary.ui.lookup.LookupScreen
import com.bluemarlin.drinkdiary.ui.lookup.LookupViewModel
import com.bluemarlin.drinkdiary.ui.profile.ProfileScreen
import com.bluemarlin.drinkdiary.ui.profile.ProfileViewModel
import com.bluemarlin.drinkdiary.ui.record.EditRecordScreen
import com.bluemarlin.drinkdiary.ui.record.EditRecordViewModel
import com.bluemarlin.drinkdiary.ui.record.RecordFlow
import com.bluemarlin.drinkdiary.ui.settings.SettingsScreen
import com.bluemarlin.drinkdiary.ui.settings.SettingsViewModel
import com.bluemarlin.drinkdiary.ui.share.ShareCardContent
import com.bluemarlin.drinkdiary.ui.share.ShareCardDialog
import com.example.ds.component.AuraAdaptiveBarState
import com.example.ds.component.AuraDialog
import com.example.ds.component.AuraDialogVariant
import com.example.ds.component.AuraIcon
import com.example.ds.component.AuraIconButton
import com.example.ds.component.AuraIconButtonVariant
import com.example.ds.component.AuraSnackbarHost
import com.example.ds.component.rememberAuraAdaptiveBarState
import com.example.ds.component.rememberAuraSnackbarHostState
import com.example.ds.foundation.theme.AuraMaterial
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private sealed interface Screen {
    data object Dashboard : Screen

    data object Collection : Screen

    data object Lookup : Screen

    data object Record : Screen

    data class Detail(
        val id: Long,
        val returnTo: DDTopLevelTab,
    ) : Screen

    data class Edit(
        val id: Long,
        val returnTo: DDTopLevelTab,
    ) : Screen

    data object Settings : Screen
}

@Composable
fun DrinkDiaryApp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as DrinkDiaryApplication).appContainer
    var screen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var shareCardContent by remember { mutableStateOf<ShareCardContent?>(null) }
    var scrollFraction by remember { mutableFloatStateOf(0f) }
    val adaptiveBarState = rememberAuraAdaptiveBarState()
    val snackbar = rememberAuraSnackbarHostState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(screen) {
        scrollFraction = 0f
        adaptiveBarState.reset()
        adaptiveBarState.updatePinnedFlags(focused = false, menuOpen = false)
    }

    LaunchedEffect(appContainer) {
        appContainer.localSeedLoader.seedIfNeeded()
    }

    val collectionViewModel: CollectionViewModel =
        viewModel(factory = CollectionViewModel.Factory(appContainer.drinkRecordRepository))
    val collection by collectionViewModel.uiState.collectAsState()

    LaunchedEffect(collection.error) {
        collection.error?.let {
            snackbar.showSnackbar(it)
            collectionViewModel.dismissError()
        }
    }

    val tabScreen: (DDTopLevelTab) -> Screen = { tab ->
        when (tab) {
            DDTopLevelTab.Dashboard -> Screen.Dashboard
            DDTopLevelTab.Collection -> Screen.Collection
            DDTopLevelTab.Search -> Screen.Lookup
        }
    }

    BackHandler(enabled = screen != Screen.Dashboard && screen != Screen.Record) {
        screen =
            when (val current = screen) {
                is Screen.Detail -> tabScreen(current.returnTo)
                is Screen.Edit -> Screen.Detail(current.id, current.returnTo)
                else -> Screen.Dashboard
            }
    }

    val host: @Composable () -> Unit = {
        val snackbarMaterial =
            if (screen is Screen.Edit || screen is Screen.Record) {
                AuraMaterial.Solid
            } else {
                AuraMaterial.Glass
            }
        AuraSnackbarHost(
            hostState = snackbar,
            material = snackbarMaterial,
        )
    }

    val openShareForScope: (TypeScope) -> Unit = { scope ->
        coroutineScope.launch {
            val profile = appContainer.observeTasteProfileUseCase(scope).first()
            when (val readiness = appContainer.resolveProfileReadinessUseCase(profile)) {
                is ProfileReadiness.Ready -> {
                    shareCardContent =
                        ShareCardContent(
                            scope = scope,
                            type = readiness.type,
                            recordCount = profile.recordCount,
                            preferences = profile.preferences,
                        )
                }
                is ProfileReadiness.NotReady -> {
                    snackbar.showSnackbar(
                        "기록이 ${readiness.recordsNeeded}개 더 모이면 취향 카드를 만들 수 있어요.",
                    )
                }
            }
        }
    }

    when (val current = screen) {
        Screen.Dashboard -> {
            val profileViewModel: ProfileViewModel =
                viewModel(
                    factory =
                        ProfileViewModel.Factory(
                            observeOverview = appContainer.observeDashboardOverviewUseCase,
                            observeTasteProfile = appContainer.observeTasteProfileUseCase,
                            observeTagPreference = appContainer.observeTagPreferenceUseCase,
                            observeRecommendations = appContainer.observeBuyRecommendationsUseCase,
                            resolveReadiness = appContainer.resolveProfileReadinessUseCase,
                        ),
                )
            val profile by profileViewModel.uiState.collectAsState()

            TopLevel(
                title = "테이스트 아카이브",
                tab = DDTopLevelTab.Dashboard,
                onTabChange = { screen = it },
                onAdd = { screen = Screen.Record },
                scrollFraction = scrollFraction,
                adaptiveBarState = adaptiveBarState,
                snackbarHost = host,
                toolbarActions = {
                    AuraIconButton(
                        onClick = { screen = Screen.Settings },
                        contentDescription = "설정",
                        variant = AuraIconButtonVariant.Ghost,
                        icon = {
                            AuraIcon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                                contentDescription = null,
                            )
                        },
                    )
                },
            ) { padding ->
                ProfileScreen(
                    state = profile,
                    onScopeChange = profileViewModel::selectScope,
                    onOpenCollection = { screen = Screen.Collection },
                    onOpenRecord = { id -> screen = Screen.Detail(id, DDTopLevelTab.Dashboard) },
                    onShareCard = { openShareForScope(profile.scope) },
                    contentPadding = padding,
                    modifier = modifier,
                    onScrollFractionChange = { scrollFraction = it },
                )
            }
        }

        Screen.Collection ->
            TopLevel(
                title = "컬렉션",
                tab = DDTopLevelTab.Collection,
                onTabChange = { screen = it },
                onAdd = { screen = Screen.Record },
                scrollFraction = scrollFraction,
                adaptiveBarState = adaptiveBarState,
                snackbarHost = host,
            ) { padding ->
                CollectionScreen(
                    state = collection,
                    onFilterChange = collectionViewModel::selectFilter,
                    onStatusFilterChange = collectionViewModel::selectStatusFilter,
                    onQueryChange = collectionViewModel::updateQuery,
                    onToggleSortByRating = collectionViewModel::toggleSortByRating,
                    onOpen = { id -> screen = Screen.Detail(id, DDTopLevelTab.Collection) },
                    contentPadding = padding,
                    modifier = modifier,
                    onScrollFractionChange = { scrollFraction = it },
                )
            }

        Screen.Lookup -> {
            val lookupViewModel: LookupViewModel =
                viewModel(
                    factory =
                        LookupViewModel.Factory(
                            repository = appContainer.drinkRecordRepository,
                            dictionary = appContainer.bottleDictionary,
                            observeRecommendations = appContainer.observeBuyRecommendationsUseCase,
                        ),
                )
            val lookup by lookupViewModel.uiState.collectAsState()

            TopLevel(
                title = "매장 찾기",
                tab = DDTopLevelTab.Search,
                onTabChange = { screen = it },
                onAdd = { screen = Screen.Record },
                scrollFraction = scrollFraction,
                adaptiveBarState = adaptiveBarState,
                snackbarHost = host,
            ) { padding ->
                LookupScreen(
                    state = lookup,
                    onQueryChange = lookupViewModel::updateQuery,
                    onOpenRecord = { id -> screen = Screen.Detail(id, DDTopLevelTab.Search) },
                    contentPadding = padding,
                    modifier = modifier,
                    onScrollFractionChange = { scrollFraction = it },
                )
            }
        }

        Screen.Record ->
            RecordFlow(
                onExit = { screen = Screen.Dashboard },
                onShareTasteCard = { type ->
                    val scope =
                        when (type) {
                            DrinkType.Wine -> TypeScope.Wine
                            DrinkType.Whiskey -> TypeScope.Whiskey
                        }
                    openShareForScope(scope)
                },
                snackbarHost = host,
                modifier = modifier,
            )

        is Screen.Detail -> {
            val record = collection.allRecords.firstOrNull { it.id == current.id }
            DDScreenScaffold(
                title = record?.let { DrinkLabels.drinkType(it.type) } ?: "기록",
                screenType = DDScreenType.Detail,
                onBackClick = { screen = tabScreen(current.returnTo) },
                scrollFraction = scrollFraction,
                adaptiveBarState = adaptiveBarState,
                snackbarHost = host,
            ) { padding ->
                RecordDetailScreen(
                    record = record,
                    onEdit = { screen = Screen.Edit(current.id, current.returnTo) },
                    onDelete = {
                        collectionViewModel.delete(current.id)
                        screen = tabScreen(current.returnTo)
                    },
                    contentPadding = padding,
                    modifier = modifier,
                    onScrollFractionChange = { scrollFraction = it },
                )
            }
        }

        is Screen.Edit -> {
            val editViewModel: EditRecordViewModel =
                viewModel(
                    key = "edit-${current.id}",
                    factory =
                        EditRecordViewModel.Factory(
                            current.id,
                            appContainer.drinkRecordRepository,
                        ),
                )
            val edit by editViewModel.uiState.collectAsState()
            var showDiscardConfirm by remember(current.id) { mutableStateOf(false) }
            val leaveEdit: () -> Unit = {
                if (edit.dirty) {
                    showDiscardConfirm = true
                } else {
                    screen = Screen.Detail(current.id, current.returnTo)
                }
            }

            // 위쪽 공용 BackHandler보다 나중에 등록되어 우선한다. 고친 내용이 있으면 먼저 묻는다.
            BackHandler(onBack = leaveEdit)

            LaunchedEffect(edit.saved) {
                if (edit.saved) screen = Screen.Detail(current.id, current.returnTo)
            }
            LaunchedEffect(edit.error) {
                edit.error?.let {
                    snackbar.showSnackbar(it)
                    editViewModel.dismissError()
                }
            }

            DDScreenScaffold(
                title = "기록 고치기",
                screenType = DDScreenType.Editor,
                onBackClick = leaveEdit,
                scrollFraction = scrollFraction,
                snackbarHost = host,
            ) { padding ->
                EditRecordScreen(
                    state = edit,
                    onFormChange = editViewModel::updateForm,
                    onAnswer = editViewModel::answer,
                    onSave = editViewModel::save,
                    contentPadding = padding,
                    modifier = modifier,
                    onScrollFractionChange = { scrollFraction = it },
                )
            }

            AuraDialog(
                visible = showDiscardConfirm,
                title = "고치기를 그만할까요?",
                message = "지금 고친 내용은 저장되지 않아요.",
                variant = AuraDialogVariant.Destructive,
                confirmText = "나가기",
                dismissText = "계속 고치기",
                onConfirm = {
                    showDiscardConfirm = false
                    screen = Screen.Detail(current.id, current.returnTo)
                },
                onDismissRequest = { showDiscardConfirm = false },
            )
        }

        Screen.Settings -> {
            val settingsViewModel: SettingsViewModel =
                viewModel(
                    factory =
                        SettingsViewModel.Factory(
                            observeOverview = appContainer.observeDashboardOverviewUseCase,
                            backupUseCase = appContainer.backupDrinkRecordsUseCase,
                        ),
                )
            val settings by settingsViewModel.uiState.collectAsState()

            LaunchedEffect(settings.message) {
                settings.message?.let {
                    snackbar.showSnackbar(it)
                    settingsViewModel.dismissMessage()
                }
            }

            DDScreenScaffold(
                title = "설정",
                screenType = DDScreenType.Detail,
                onBackClick = { screen = Screen.Dashboard },
                scrollFraction = scrollFraction,
                adaptiveBarState = adaptiveBarState,
                snackbarHost = host,
            ) { padding ->
                SettingsScreen(
                    state = settings,
                    onExportBackup = { uri ->
                        settingsViewModel.exportToUri(context.contentResolver, uri)
                    },
                    onRestoreBackup = { uri ->
                        settingsViewModel.restoreFromUri(context.contentResolver, uri)
                    },
                    contentPadding = padding,
                    modifier = modifier,
                    onScrollFractionChange = { scrollFraction = it },
                )
            }
        }
    }

    ShareCardDialog(
        content = shareCardContent,
        onDismiss = { shareCardContent = null },
    )
}

@Composable
private fun TopLevel(
    title: String,
    tab: DDTopLevelTab,
    onTabChange: (Screen) -> Unit,
    onAdd: () -> Unit,
    scrollFraction: Float,
    adaptiveBarState: AuraAdaptiveBarState,
    snackbarHost: @Composable () -> Unit,
    toolbarActions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    DDScreenScaffold(
        title = title,
        screenType = DDScreenType.TopLevel,
        selectedTab = tab,
        onDashboardClick = { onTabChange(Screen.Dashboard) },
        onCollectionClick = { onTabChange(Screen.Collection) },
        onSearchClick = { onTabChange(Screen.Lookup) },
        onAddClick = onAdd,
        scrollFraction = scrollFraction,
        adaptiveBarState = adaptiveBarState,
        toolbarActions = toolbarActions,
        snackbarHost = snackbarHost,
        content = content,
    )
}
