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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bluemarlin.drinkdiary.DrinkDiaryApplication
import com.bluemarlin.drinkdiary.R
import com.bluemarlin.drinkdiary.domain.model.DrinkType
import com.bluemarlin.drinkdiary.domain.model.ProfileReadiness
import com.bluemarlin.drinkdiary.domain.model.TypeScope
import com.bluemarlin.drinkdiary.ui.collection.CollectionFilterAction
import com.bluemarlin.drinkdiary.ui.collection.CollectionScreen
import com.bluemarlin.drinkdiary.ui.collection.CollectionViewModel
import com.bluemarlin.drinkdiary.ui.collection.RecordDetailScreen
import com.bluemarlin.drinkdiary.ui.collection.collectionFilterSummary
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
import com.example.ds.component.AuraSnackbarHost
import com.example.ds.component.rememberAuraAdaptiveBarState
import com.example.ds.component.rememberAuraSnackbarHostState
import com.example.ds.foundation.theme.AuraMaterial
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private sealed interface Screen {
    data object Dashboard : Screen

    data object Collection : Screen

    // 일지 검색에서 못 찾은 이름으로 들어오면 그 이름을 채워 둔다.
    data class Record(
        val prefillName: String = "",
    ) : Screen

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
    val adaptiveBarState = rememberAuraAdaptiveBarState(minimizeOnScrollDown = true)
    val snackbar = rememberAuraSnackbarHostState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(screen) {
        scrollFraction = 0f
        adaptiveBarState.reset()
        adaptiveBarState.updatePinnedFlags(focused = false, menuOpen = false)
    }

    LaunchedEffect(appContainer) {
        appContainer.localSeedLoader.seedIfNeeded()
        appContainer.pruneRecordPhotosUseCase()
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
            DDTopLevelTab.Settings -> Screen.Settings
        }
    }

    BackHandler(enabled = screen != Screen.Dashboard && screen !is Screen.Record) {
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
                        "${readiness.recordsNeeded}잔 더 기록하면 취향 카드를 만들 수 있어요.",
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
                            observeHighlights = appContainer.observeLogHighlightsUseCase,
                            observeTasteProfile = appContainer.observeTasteProfileUseCase,
                            observeTagPreference = appContainer.observeTagPreferenceUseCase,
                            observeRecommendations = appContainer.observeBuyRecommendationsUseCase,
                            resolveReadiness = appContainer.resolveProfileReadinessUseCase,
                        ),
                )
            val profile by profileViewModel.uiState.collectAsState()

            TopLevel(
                title = stringResource(R.string.nav_dashboard),
                tab = DDTopLevelTab.Dashboard,
                onTabChange = { screen = it },
                onAdd = { screen = Screen.Record() },
                scrollFraction = scrollFraction,
                adaptiveBarState = adaptiveBarState,
                snackbarHost = host,
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
                title = stringResource(R.string.nav_collection),
                subtitle = collectionFilterSummary(collection),
                tab = DDTopLevelTab.Collection,
                onTabChange = { screen = it },
                onAdd = { screen = Screen.Record() },
                scrollFraction = scrollFraction,
                adaptiveBarState = adaptiveBarState,
                snackbarHost = host,
                toolbarActions = {
                    CollectionFilterAction(
                        state = collection,
                        onTypeChange = collectionViewModel::selectFilter,
                        onStatusChange = collectionViewModel::selectStatusFilter,
                        onSortByRatingChange = collectionViewModel::selectSortByRating,
                    )
                },
            ) { padding ->
                CollectionScreen(
                    state = collection,
                    onQueryChange = collectionViewModel::updateQuery,
                    onOpen = { id -> screen = Screen.Detail(id, DDTopLevelTab.Collection) },
                    onRecordNew = { name -> screen = Screen.Record(prefillName = name) },
                    contentPadding = padding,
                    modifier = modifier,
                    onScrollFractionChange = { scrollFraction = it },
                )
            }

        is Screen.Record ->
            RecordFlow(
                prefillName = current.prefillName,
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
                title = record?.name ?: "기록",
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
                            appContainer.recordPhotoStore,
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
                    onPhotoPicked = editViewModel::pickPhoto,
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

            TopLevel(
                title = stringResource(R.string.nav_settings),
                tab = DDTopLevelTab.Settings,
                onTabChange = { screen = it },
                onAdd = { screen = Screen.Record() },
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
    subtitle: String? = null,
    toolbarActions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    DDScreenScaffold(
        title = title,
        screenType = DDScreenType.TopLevel,
        subtitle = subtitle,
        selectedTab = tab,
        onDashboardClick = { onTabChange(Screen.Dashboard) },
        onCollectionClick = { onTabChange(Screen.Collection) },
        onSettingsClick = { onTabChange(Screen.Settings) },
        onAddClick = onAdd,
        scrollFraction = scrollFraction,
        adaptiveBarState = adaptiveBarState,
        toolbarActions = toolbarActions,
        snackbarHost = snackbarHost,
        content = content,
    )
}
