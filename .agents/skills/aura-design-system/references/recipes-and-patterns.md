# Aura Consumer Screen & Component Recipes

All recipes below compile against `com.example.ds` using zero Material 3 widgets and zero hardcoded color, font-size, or motion literals.

---

## 1. Root App Setup (`Standard` Global + `Dark/Light` Auto)

```kotlin
@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    AuraTheme(
        colorMode = if (darkTheme) AuraColorMode.Dark else AuraColorMode.Light,
        material = AuraMaterial.Solid, // Keep Solid default for workspace/content clarity
        motionProfile = AuraMotionProfile.Standard,
        content = content,
    )
}
```

---

## 2. Spatial Feed Screen with `FloatingGlassScaffold` & `LiquidGlass` Hero Island

Use for primary top-level tabs where scrollable content should visibly glide beneath floating navigation chrome (`<= 1` `LiquidGlass` hero + `<= 2` `Glass` chrome surfaces = within the 3-blur budget).

```kotlin
@Composable
fun CatalogHomeScreen(
    items: List<String>,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    onAddClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    val scrollProgress = rememberFloatingScrollProgress(listState)
    val layout = AuraTheme.layout

    FloatingGlassScaffold(
        topBar = { _ ->
            FloatingTopPillBar(
                title = "Library",
                scrollProgress = scrollProgress,
                glassStyle = GlassStyle.FROSTED, // Frosted blur so BottomIsland stays the single LiquidGlass hero
                onSearchClick = onSearchClick,
            )
        },
        bottomBar = { _ ->
            AuraTheme(motionProfile = AuraMotionProfile.Expressive) { // <= 1 Expressive hero on screen
                FloatingBottomIsland(
                    items = listOf(
                        AuraBottomIslandItem("home", "Home", Icons.Outlined.Home, Icons.Filled.Home),
                        AuraBottomIslandItem("stats", "Stats", Icons.Outlined.Info, Icons.Filled.Info),
                    ),
                    selectedIndex = selectedTab,
                    onItemSelected = onSelectTab,
                    glassStyle = GlassStyle.LIQUID,
                    splitActionIcon = Icons.Filled.Add,
                    splitActionLabel = "Add",
                    onSplitActionClick = onAddClick,
                )
            }
        },
    ) { contentPadding ->
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = layout.screenMarginHorizontal,
                end = layout.screenMarginHorizontal,
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(layout.gridGutter),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items) { label ->
                // Layer 1 content cards ALWAYS use Solid variants (Outlined / Filled / Elevated)
                AuraCard(
                    variant = AuraCardVariant.Outlined,
                    onClick = { /* open detail */ },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AuraText(text = label, style = AuraTheme.typography.titleMedium)
                    AuraText(
                        text = "Secondary metadata",
                        style = AuraTheme.typography.bodySmall,
                        color = AuraTheme.colors.content.secondary,
                    )
                }
            }
        }
    }
}
```

---

## 3. Responsive Form / Detail Screen (`AuraScaffold` + `maxContentWidth`)

Use for record creation, settings, or structured detail views where readability and focused task efficiency (`Standard` motion track) matter most.

```kotlin
@Composable
fun RecordEditorScreen(
    name: String,
    onNameChange: (String) -> Unit,
    abvText: String,
    onAbvChange: (String) -> Unit,
    rating: Float,
    onRatingChange: (Float) -> Unit,
    favorite: Boolean,
    onFavoriteChange: (Boolean) -> Unit,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val layout = AuraTheme.layout

    AuraScaffold(
        topBar = {
            AuraTopAppBar(
                title = "Edit Entry",
                style = AuraTopAppBarStyle.SurfaceDocked,
                surface = AuraTopAppBarSurface.SolidElevated,
                navigationIcon = {
                    AuraIconButton(
                        onClick = onBack,
                        contentDescription = "Navigate back",
                    ) {
                        AuraIcon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = layout.maxContentWidth)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = layout.screenMarginHorizontal,
                        vertical = layout.screenMarginVertical,
                    ),
                verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
            ) {
                AuraTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "Name",
                    placeholder = "Enter title",
                    modifier = Modifier.fillMaxWidth(),
                )
                AuraNumberField(
                    value = abvText,
                    onValueChange = onAbvChange,
                    label = "ABV",
                    suffixText = "%",
                    showSteppers = true,
                    step = 0.5,
                    modifier = Modifier.fillMaxWidth(),
                )
                AuraRatingBar(
                    rating = rating,
                    onRatingChange = onRatingChange,
                    label = "Tasting Score",
                )
                AuraSwitch(
                    checked = favorite,
                    onCheckedChange = onFavoriteChange,
                    label = "Mark as Favorite",
                    description = "Pinned to top collection",
                )
                AuraButton(
                    text = "Save Entry",
                    onClick = onSave,
                    loading = isSaving,
                    fullWidth = true,
                    size = AuraButtonSize.Large,
                )
            }
        }
    }
}
```

---

## 4. Adaptive Tablet / Foldable Layout (`AuraAdaptiveTwoPane` & `AuraAdaptiveGrid`)

```kotlin
@Composable
fun MasterDetailCatalog(
    categories: List<String>,
    selectedCategory: String?,
    onSelectCategory: (String) -> Unit,
) {
    val layout = AuraTheme.layout

    AuraAdaptiveTwoPane(
        modifier = Modifier.fillMaxSize(),
        primaryPane = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(layout.gridGutter),
                contentPadding = PaddingValues(layout.screenMarginHorizontal),
            ) {
                items(categories) { category ->
                    AuraCard(
                        variant = AuraCardVariant.Filled,
                        selected = category == selectedCategory,
                        onClick = { onSelectCategory(category) },
                    ) {
                        AuraText(text = category, style = AuraTheme.typography.titleMedium)
                    }
                }
            }
        },
        secondaryPane = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(layout.screenMarginHorizontal),
                verticalArrangement = Arrangement.spacedBy(layout.sectionSpacing),
            ) {
                AuraText(
                    text = selectedCategory ?: "Select a category",
                    style = AuraTheme.typography.headlineMedium,
                )
                AuraAdaptiveGrid(items = listOf("Item A", "Item B", "Item C")) { item ->
                    AuraCard(variant = AuraCardVariant.Outlined) {
                        AuraText(text = item, style = AuraTheme.typography.bodyLarge)
                    }
                }
            }
        },
    )
}
```

---

## 5. Modal Dialog & `Expressive` Scope

```kotlin
AuraTheme(motionProfile = AuraMotionProfile.Expressive) {
    AuraDialog(
        visible = showDeleteConfirm,
        title = "Delete Tasting Note?",
        message = "This action permanently removes the tasting note from your diary.",
        variant = AuraDialogVariant.Destructive,
        confirmText = "Delete",
        dismissText = "Cancel",
        onConfirm = { /* confirm delete */ },
        onDismissRequest = { showDeleteConfirm = false },
    )
}
```

---

## 6. Compliant App-Specific Gap Components

When a consumer app needs a component outside Aura (e.g., an inline divider or progress indicator), build an app-prefixed composable using **only** `AuraTheme.*`:

```kotlin
@Composable
fun AppDivider(
    modifier: Modifier = Modifier,
    strong: Boolean = false,
) {
    val color = if (strong) AuraTheme.colors.border.default else AuraTheme.colors.border.subtle
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color),
    )
}

@Composable
fun AppStatusNotice(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    AuraCard(
        modifier = modifier.fillMaxWidth(),
        variant = AuraCardVariant.Filled,
        padding = AuraCardPadding.Compact,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AuraTheme.layout.gridGutter),
        ) {
            AuraText(
                text = message,
                style = AuraTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (actionLabel != null && onAction != null) {
                AuraButton(
                    text = actionLabel,
                    onClick = onAction,
                    variant = AuraButtonVariant.Ghost,
                    size = AuraButtonSize.Small,
                )
            }
        }
    }
}
```
