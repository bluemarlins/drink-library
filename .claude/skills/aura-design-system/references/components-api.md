# Aura Component API Reference

All components live in `package io.github.bluemarlins.aura.component`. Call these composables directly—do **not** fall back to Material 3 equivalents.

> **Note on `GlassStyle`:** `GlassStyle` (`FROSTED` = `AuraMaterial.Glass`, `LIQUID` = `AuraMaterial.LiquidGlass`) is `@Deprecated` in favor of `AuraMaterial` and `GlassStyle.toMaterial()`. `GlassSurface` and `LiquidMetaballBridge` take `material: AuraMaterial` (their `glassStyle` overloads are deprecated), though `FloatingBottomIsland`, `AuraBottomIsland`, `FloatingTopPillBar`, `MorphingMorePopover`, `AuraDialog`, and `AuraSearchBar` keep `glassStyle` parameters for compatibility (expect a deprecation warning when passing one). Every surface resolves its final rendering through `AuraTheme.effectiveMaterial(...)`, which never exceeds the `AuraTheme(material = ...)` ceiling.

---

## 1. Core Primitives

### `AuraText` & `ProvideAuraContentColorTextStyle`
```kotlin
val LocalAuraContentColor: ProvidableCompositionLocal<Color>
val LocalAuraTextStyle: ProvidableCompositionLocal<TextStyle>

@Composable
fun ProvideAuraContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle = LocalAuraTextStyle.current,
    content: @Composable () -> Unit,
)

@Composable
fun AuraText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified, // Resolves: explicit color -> style.color -> LocalAuraContentColor -> colors.content.primary
    style: TextStyle = LocalAuraTextStyle.current,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
)
```

### `AuraIcon`
```kotlin
@Composable
fun AuraIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalAuraContentColor.current.takeOrElse { AuraTheme.colors.content.primary },
)

@Composable
fun AuraIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalAuraContentColor.current.takeOrElse { AuraTheme.colors.content.primary },
)
```

---

## 2. Layout & Spatial Scaffolds

### `AuraScaffold`
```kotlin
@Composable
fun AuraScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = AuraTheme.colors.background.primary,
    contentColor: Color = AuraTheme.colors.content.primary,
    contentWindowInsets: WindowInsets = WindowInsets.systemBars,
    content: @Composable (PaddingValues) -> Unit,
)
```

### `AuraAdaptiveTwoPane` & `AuraAdaptiveGrid`
```kotlin
@Composable
fun AuraAdaptiveTwoPane(
    modifier: Modifier = Modifier,
    forceSinglePane: Boolean = false,
    primaryPaneFraction: Float = AuraTheme.layout.primaryPaneFraction,
    paneSpacing: Dp = AuraTheme.layout.gridGutter,
    primaryPane: @Composable () -> Unit,
    secondaryPane: @Composable () -> Unit,
)

@Composable
fun <T> AuraAdaptiveGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = AuraTheme.layout.gridColumns,
    horizontalSpacing: Dp = AuraTheme.layout.gridGutter,
    verticalSpacing: Dp = AuraTheme.layout.gridGutter,
    itemContent: @Composable (T) -> Unit,
)
```

### `FloatingGlassScaffold` & Scroll/Backdrop Helpers
```kotlin
@Composable
fun FloatingGlassScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable BoxScope.(GlassBackdropState) -> Unit = {},
    bottomBar: @Composable BoxScope.(GlassBackdropState) -> Unit = {},
    overlay: @Composable BoxScope.(GlassBackdropState) -> Unit = {},
    overlayVisible: Boolean = false,
    onDismissOverlay: () -> Unit = {},
    hasBottomIsland: Boolean = true,
    bottomIslandHeight: Dp = 60.dp,
    bottomIslandMargin: Dp = 16.dp,
    snackbarHost: @Composable () -> Unit = {},
    containerColor: Color = AuraTheme.colors.background.primary,
    contentColor: Color = AuraTheme.colors.content.primary,
    adaptiveBarState: AuraAdaptiveBarState? = null,
    topBarClearance: Dp = ..., // 64.dp; with adaptiveBarState: 112.dp (48.dp on Compact height)
    content: @Composable (PaddingValues) -> Unit,
)

@Composable
fun rememberFloatingScrollProgress(listState: LazyListState, threshold: Dp = 64.dp): Float

@Composable
fun rememberFloatingScrollProgress(scrollState: ScrollState, threshold: Dp = 64.dp): Float

fun calculateBottomIslandContentPadding(
    navBarBottomInset: Dp,
    islandHeight: Dp = 60.dp,
    bottomMargin: Dp = 16.dp,
    extraBreathing: Dp = 16.dp,
): Dp
```

### `GlassSurface` & Optical Helpers
```kotlin
@Composable fun rememberGlassBackdropState(): GlassBackdropState
val LocalGlassBackdropState: ProvidableCompositionLocal<GlassBackdropState?>
fun Modifier.glassBackdropSource(state: GlassBackdropState): Modifier
fun Modifier.fluidPressScale(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    pressedScale: Float? = null, // null -> AuraTheme.motion.glassPressedScale (0.95f)
): Modifier

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    material: AuraMaterial = AuraTheme.material,
    shape: Shape = RoundedCornerShape(50),
    cornerRadiusHint: Dp = 28.dp,
    backdropState: GlassBackdropState? = LocalGlassBackdropState.current,
    dispersionBoost: Float = 1f,
    surfaceAlphaMultiplier: Float = 1f,
    showShadow: Boolean = true,
    elevation: AuraElevationLevel = AuraTheme.elevation.level2,
    blurRadius: Dp = AuraTheme.glass.blurRadius,
    saturationBoost: Float = AuraTheme.glass.saturation,
    tintColorOverride: Color? = null,
    borderColorOverride: Color? = null,
    borderWidth: Dp = 1.dp,
    elevationAlphaMultiplier: Float = 1f,
    content: @Composable BoxScope.() -> Unit,
)

@Composable
fun LiquidMetaballBridge(
    gapWidthDp: Dp,
    heightDp: Dp,
    material: AuraMaterial,
    mergeProgress: Float,
    modifier: Modifier = Modifier,
)
```

---

## 3. Navigation Chrome

### `AuraTopAppBar`, `FloatingTopPillBar` & `MorphingMorePopover`
```kotlin
enum class AuraTopAppBarStyle { FloatingCapsule, FloatingSplit, SurfaceDocked }
enum class AuraTopAppBarSurface { SolidElevated, TranslucentGlass }
enum class AuraTopAppBarScrollBehavior { Pinned, EnterAlways, CompactOnScroll, AdaptiveFloating }
enum class AdaptiveFloatingBarPhase { DockedExpanded, CollapsingTransition, FloatingVisible, FloatingHidden, FloatingPinned, CompactDocked }
data class AuraMoreMenuAction(val id: String, val label: String)

@Composable
fun rememberAuraAdaptiveBarState(
    collapseThreshold: Dp = 160.dp,
    hideScrollDelta: Dp = 24.dp,
    revealScrollDelta: Dp = 12.dp,
    jitterSlop: Dp = 12.dp,
): AuraAdaptiveBarState

@Composable fun BindAdaptiveBarToScrollState(scrollState: ScrollState, adaptiveState: AuraAdaptiveBarState? = LocalAuraAdaptiveBarState.current)
@Composable fun BindAdaptiveBarToLazyListState(listState: LazyListState, adaptiveState: AuraAdaptiveBarState? = LocalAuraAdaptiveBarState.current)
@Composable fun BindAdaptiveBarToLazyGridState(gridState: LazyGridState, adaptiveState: AuraAdaptiveBarState? = LocalAuraAdaptiveBarState.current)

@Composable
fun AuraTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    style: AuraTopAppBarStyle = AuraTopAppBarStyle.FloatingCapsule,
    surface: AuraTopAppBarSurface = AuraTopAppBarSurface.SolidElevated,
    scrollBehavior: AuraTopAppBarScrollBehavior = AuraTopAppBarScrollBehavior.Pinned,
    scrollFraction: Float = 0f,
    centerTitle: Boolean = false,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    subContent: (@Composable () -> Unit)? = null,
    adaptiveState: AuraAdaptiveBarState? = LocalAuraAdaptiveBarState.current,
    expandedTitle: String? = null,
    expandedSubtitle: String? = null,
    inlineExpandedTitle: Boolean = false, // 1.2.0: large title shares the action row
)

@Composable
fun FloatingTopPillBar(
    title: String,
    scrollProgress: Float,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    glassStyle: GlassStyle = GlassStyle.LIQUID,
    isScrollingDown: Boolean = false,
    hideTitleOnScrollDown: Boolean = true,
    metaballSplitProgress: Float = 0f,
    onBackClick: (() -> Unit)? = null,
    onSearchClick: (() -> Unit)? = null,
    isBookmarked: Boolean = false,
    onBookmarkClick: (() -> Unit)? = null,
    moreMenuExpanded: Boolean = false,
    onMoreMenuExpandedChange: (Boolean) -> Unit = {},
)

@Composable
fun MorphingMorePopover(
    expanded: Boolean,
    actions: List<AuraMoreMenuAction>,
    onActionClick: (AuraMoreMenuAction) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    glassStyle: GlassStyle = GlassStyle.LIQUID,
)
```

### `AuraDropdownMenu` (1.2.0)
Anchored overflow / filter menu. Put it in the trigger's parent `Box`; it opens below the trigger, end-aligned, and flips above when there is no room. One `AuraMenuSection` per decision, one `selected` item per filter section.
```kotlin
data class AuraMenuItem(val id: String, val label: String, val selected: Boolean = false, val enabled: Boolean = true)
data class AuraMenuSection(val title: String? = null, val items: List<AuraMenuItem>)

@Composable
fun AuraDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    sections: List<AuraMenuSection>,
    onItemClick: (AuraMenuItem) -> Unit,
    modifier: Modifier = Modifier,
    dismissOnItemClick: Boolean = true,
)
```

### `AuraFloatingActionBar` (1.3.0)
Floating glass action dock for task screens (create/edit forms, wizards, detail). Holds one `Primary` `AuraButton` plus at most one `Secondary`/`Ghost`; rides above the IME. Use it instead of a trailing inline Save button.
```kotlin
@Composable
fun AuraFloatingActionBar(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    material: AuraMaterial = AuraMaterial.Glass,
    supportingText: String? = null, // e.g. why Save is disabled
    content: @Composable RowScope.() -> Unit,
)

// FloatingGlassScaffold(
//     bottomBar = { AuraFloatingActionBar { AuraButton("Save", onSave, Modifier.weight(1f), fullWidth = true) } },
//     hasBottomIsland = true,
//     bottomIslandHeight = AuraFloatingActionBarDefaults.Height, // + SupportingTextHeight when supportingText is set
//     bottomIslandMargin = AuraFloatingActionBarDefaults.BottomMargin,
// ) { padding -> ... }
```

### `AuraBottomIsland` & `FloatingBottomIsland`
```kotlin
data class AuraBottomIslandItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
)

@Composable
fun AuraBottomIsland(
    items: List<AuraBottomIslandItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    glassStyle: GlassStyle = GlassStyle.FROSTED,
    splitActionIcon: ImageVector? = null,
    splitActionLabel: String? = null,
    isActionSplit: Boolean = true,
    onSplitActionClick: (() -> Unit)? = null,
)

@Composable
fun FloatingBottomIsland(
    items: List<AuraBottomIslandItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    glassStyle: GlassStyle = GlassStyle.LIQUID,
    splitActionIcon: ImageVector? = null,
    splitActionLabel: String? = null,
    isActionSplit: Boolean = true,
    onSplitActionClick: (() -> Unit)? = null,
)
```

---

## 4. Actions

### `AuraButton`
```kotlin
enum class AuraButtonVariant { Primary, Secondary, Outline, Ghost, Destructive }
enum class AuraButtonSize { Small, Medium, Large }

@Composable
fun AuraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraButtonVariant = AuraButtonVariant.Primary,
    size: AuraButtonSize = AuraButtonSize.Medium,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false,
    debounceIntervalMs: Long = 400L,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
)
```

### `AuraIconButton`
```kotlin
enum class AuraIconButtonVariant { Filled, Tonal, Outlined, Ghost, Floating, Destructive }
enum class AuraIconButtonSize { Small, Medium, Large }
enum class AuraIconButtonShape { Circle, RoundedSquare }

@Composable
fun AuraIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraIconButtonVariant = AuraIconButtonVariant.Ghost,
    size: AuraIconButtonSize = AuraIconButtonSize.Medium,
    shape: AuraIconButtonShape = AuraIconButtonShape.Circle,
    enabled: Boolean = true,
    loading: Boolean = false,
    selected: Boolean? = null,
    contentDescription: String? = null,
    debounceIntervalMs: Long = 300L,
    interactionSource: MutableInteractionSource? = null,
    icon: @Composable () -> Unit,
)
```

### `AuraFab`
```kotlin
enum class AuraFabVariant { Primary, Secondary, Surface, Glass }
enum class AuraFabSize { Small, Regular, Large }

@Composable
fun AuraFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    expanded: Boolean = true,
    variant: AuraFabVariant = AuraFabVariant.Primary,
    size: AuraFabSize = AuraFabSize.Regular,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentDescription: String? = null,
    debounceIntervalMs: Long = 400L,
    interactionSource: MutableInteractionSource? = null,
    icon: @Composable () -> Unit,
)
```

### `AuraSegmentedButton`
```kotlin
enum class AuraSegmentedVariant { Surface, Outlined, Glass }
enum class AuraSegmentedSize { Small, Medium, Large }

@Composable
fun AuraSegmentedButton(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraSegmentedVariant = AuraSegmentedVariant.Surface,
    size: AuraSegmentedSize = AuraSegmentedSize.Medium,
    enabled: Boolean = true,
    fullWidth: Boolean = true,
    optionIcons: List<(@Composable () -> Unit)?>? = null,
)
```

### `AuraChipButton`
```kotlin
enum class AuraChipVariant { Filter, Tag, Assist, Glass }
enum class AuraChipSize { Small, Medium }

@Composable
fun AuraChipButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    variant: AuraChipVariant = AuraChipVariant.Filter,
    size: AuraChipSize = AuraChipSize.Medium,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    debounceIntervalMs: Long = 200L,
    interactionSource: MutableInteractionSource? = null,
)
```

---

## 5. Inputs & Selection

### `AuraTextField` & `AuraNumberField`
```kotlin
enum class AuraTextFieldVariant { Outlined, Filled, Paperless }

@Composable
fun AuraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraTextFieldVariant = AuraTextFieldVariant.Outlined,
    label: String? = null,
    placeholder: String? = null,
    supportingText: String? = null,
    errorText: String? = null,
    prefixText: String? = null,
    suffixText: String? = null,
    maxLength: Int? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
)

@Composable
fun AuraNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraTextFieldVariant = AuraTextFieldVariant.Outlined,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    prefixText: String? = null,
    suffixText: String? = null,
    supportingText: String? = null,
    errorMessage: String? = null,
    showSteppers: Boolean = false,
    step: Double = 1.0,
    minValue: Double? = null,
    maxValue: Double? = null,
    allowDecimals: Boolean = true,
    formatThousands: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
)
```

### `AuraSearchBar` & `AuraDateTimePicker`
```kotlin
enum class AuraSearchBarSurface { Solid, Glass }

@Composable
fun AuraSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearch: ((String) -> Unit)? = null,
    placeholder: String = "Search...",
    surface: AuraSearchBarSurface = AuraSearchBarSurface.Solid,
    glassStyle: GlassStyle = GlassStyle.FROSTED,
    enabled: Boolean = true,
    activeFilterCount: Int = 0,
    onFilterClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
)

enum class AuraDateTimeMode { Date, DateTime, YearVintage }

@Composable
fun AuraDateTimePicker(
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onDateChange: (Int, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    mode: AuraDateTimeMode = AuraDateTimeMode.Date,
    selectedHour: Int = 19,
    selectedMinute: Int = 0,
    onTimeChange: ((Int, Int) -> Unit)? = null,
    enabled: Boolean = true,
    label: String? = null,
    supportingText: String? = null,
    minYear: Int = 1950,
    maxYear: Int = 2030,
)
```

### `AuraSwitch`, `AuraCheckbox`, `AuraRadio`, `AuraSlider`, `AuraRatingBar`
```kotlin
enum class AuraSwitchSize { Medium, Small }

@Composable
fun AuraSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    size: AuraSwitchSize = AuraSwitchSize.Medium,
    enabled: Boolean = true,
    label: String? = null,
    description: String? = null,
)

enum class AuraCheckboxState { Unchecked, Checked, Indeterminate }

@Composable
fun AuraCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    indeterminate: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    label: String? = null,
    description: String? = null,
    strikethroughWhenChecked: Boolean = false,
)

@Composable
fun AuraRadio(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    description: String? = null,
)

@Composable
fun AuraSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    label: String? = null,
    valueLabel: String? = null,
    supportingText: String? = null,
    interactionSource: MutableInteractionSource? = null,
)

enum class AuraRatingBarSize { Small, Medium, Large }

@Composable
fun AuraRatingBar(
    rating: Float,
    onRatingChange: ((Float) -> Unit)?,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    allowHalfRating: Boolean = true,
    size: AuraRatingBarSize = AuraRatingBarSize.Medium,
    enabled: Boolean = true,
    label: String? = null,
    showValueText: Boolean = true,
)
```

---

## 6. Containment & Feedback

### `AuraCard`, `AuraDialog`, `AuraBadge`, `AuraBadgedBox`
```kotlin
enum class AuraCardVariant { Elevated, Filled, Outlined, Glass }
enum class AuraCardPadding { None, Compact, Medium, Large }

@Composable
fun AuraCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    variant: AuraCardVariant = AuraCardVariant.Outlined,
    padding: AuraCardPadding = AuraCardPadding.Medium,
    enabled: Boolean = true,
    selected: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
)

enum class AuraDialogVariant { Standard, Destructive, Glass }

@Composable
fun AuraDialog(
    visible: Boolean,
    title: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AuraDialogVariant = AuraDialogVariant.Standard,
    glassStyle: GlassStyle = GlassStyle.FROSTED,
    message: String? = null,
    confirmText: String = "Confirm",
    dismissText: String? = "Cancel",
    confirmEnabled: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    content: (@Composable () -> Unit)? = null,
)

enum class AuraBadgeVariant { Brand, Neutral, Success, Warning, Error, Info }
enum class AuraBadgeSize { Dot, Small, Medium }

@Composable
fun AuraBadge(
    modifier: Modifier = Modifier,
    label: String? = null,
    count: Int? = null,
    maxCount: Int = 99,
    variant: AuraBadgeVariant = AuraBadgeVariant.Brand,
    size: AuraBadgeSize = AuraBadgeSize.Medium,
)

@Composable
fun AuraBadgedBox(
    badge: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
)

enum class AuraDividerOrientation { Horizontal, Vertical }
enum class AuraDividerTone { Subtle, Default, Strong }

@Composable
fun AuraDivider(
    modifier: Modifier = Modifier,
    orientation: AuraDividerOrientation = AuraDividerOrientation.Horizontal,
    tone: AuraDividerTone = AuraDividerTone.Subtle,
    thickness: Dp = 1.dp,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp,
)

enum class AuraProgressVariant { Brand, Success, Warning, Error, Info }

@Composable
fun AuraLinearProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    variant: AuraProgressVariant = AuraProgressVariant.Brand,
    height: Dp = 6.dp,
    shape: Shape = AuraTheme.shapes.small,
    trackColor: Color = AuraTheme.colors.surface.sunken,
    indicatorColor: Color = Color.Unspecified,
    contentDescription: String? = null,
)

@Composable
fun AuraStepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    variant: AuraProgressVariant = AuraProgressVariant.Brand,
    height: Dp = 4.dp,
    segmentSpacing: Dp = 4.dp,
    shape: Shape = AuraTheme.shapes.extraSmall,
    activeColor: Color = Color.Unspecified,
    inactiveColor: Color = AuraTheme.colors.surface.sunken,
    contentDescription: String? = null,
)

enum class AuraSnackbarVariant { Neutral, Success, Warning, Error }
enum class AuraSnackbarDuration(val durationMillis: Long) { Short(3_000L), Long(5_000L), Indefinite(kotlin.Long.MAX_VALUE) }
enum class AuraSnackbarResult { Dismissed, ActionPerformed }

@Composable
fun rememberAuraSnackbarHostState(): AuraSnackbarHostState

@Composable
fun AuraSnackbarHost(
    hostState: AuraSnackbarHostState,
    modifier: Modifier = Modifier,
    material: AuraMaterial = AuraMaterial.Solid,
    snackbar: @Composable (AuraSnackbarData) -> Unit = ...,
)

@Composable
fun AuraSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    variant: AuraSnackbarVariant = AuraSnackbarVariant.Neutral,
    material: AuraMaterial = AuraMaterial.Solid,
    shape: Shape = AuraTheme.shapes.medium,
)

// 1.3.0 — loading placeholders. Render until the first data emission; never show empty-state copy while loading.
@Composable
fun AuraSkeleton(
    modifier: Modifier = Modifier,
    shape: Shape = AuraTheme.shapes.small,
    contentDescription: String? = null,
)

@Composable
fun AuraSkeletonText(
    modifier: Modifier = Modifier,
    lines: Int = 2,
    lineHeight: Dp = 14.dp,
    lineSpacing: Dp = 8.dp,
    contentDescription: String? = null,
)
```
