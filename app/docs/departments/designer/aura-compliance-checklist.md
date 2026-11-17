# Aura Design System 준수 체크리스트 (`drink-library`)

- **도출 소스:** `aura-design-system` `v1.1.0` (`docs/foundation/00`–`07`, `docs/components/00-spec-architecture.md`, `01-component-evaluation-ruleset.md`, 카테고리 원칙 5종, 컴포넌트 스펙 24종)
- **대상 앱:** `drink-library` (`app` 모듈, `app/libs/aura-design-system-1.1.0.aar`)
- **최종 작성일:** 2026-11-16
- **상태:** 초안 (`departments/designer` → 검증·개선 완료 후 `specs/designer/` 승격 대상)
- **판정 기준:** `PASS` | `FAIL` | `N/A` (미사용 컴포넌트의 경우 도입 시 적용할 기준만 유지; 예외(`WAIVED`) 불허)

---

## 1. Tier A — 시스템 공통 게이트 (`Foundation` & `EVAL-001`–`EVAL-008`)

### 1.1 의존성 및 토큰 무결성 (`EVAL-002`, `01-token-architecture`, `02-color`, `04-typography`)

- [ ] **`CHK-SYS-001` (Zero Material 3):** `app/build.gradle.kts`, `libs.versions.toml`, `app/src/main` Kotlin 코드 전체에 `androidx.compose.material3` 및 `MaterialTheme` import/사용이 **0건**이다. *(참고: 아이콘 벡터 리소스/ImageVector 자체는 허용)*
- [ ] **`CHK-SYS-002` (Color Token Only):** 프로덕션 UI 코드(`@Preview` 제외)에 `Color(0x...)` 리터럴이 0건이며, 모든 색상은 `AuraTheme.colors.*` (`background`, `surface`, `content`, `brand`, `border`, `status`, `interaction`) 및 `LocalAuraContentColor.current`만 사용한다.
- [ ] **`CHK-SYS-003` (Typography Token & `AuraText`):** 프로덕션 UI 코드에 `.sp` 하드코딩 리터럴이 0건이며, 모든 텍스트는 `AuraText` + `AuraTheme.typography.*` (16개 역할, Sans-only)를 사용한다.
- [ ] **`CHK-SYS-004` (Shape & Elevation Token):** 모서리 반경은 `AuraTheme.shapes.*` (`extraSmall`, `small`, `medium`, `large`, `extraLarge`, `full`)를 사용하고, 그림자·스크림 투명도는 `AuraTheme.elevation.level0`–`level4`만 사용한다 (`VIS-RULE-005`).
- [ ] **`CHK-SYS-005` (Root Theme Entry):** 앱 루트(`DrinkDiaryTheme`)가 `AuraTheme(colorMode = ..., material = AuraMaterial.Solid, motionProfile = AuraMotionProfile.Standard)`를 명시적으로 구성해 Light/Dark 및 기본 트랙을 제공한다.

### 1.2 4-Layer Visual & Materiality 규율 (`VIS-RULE-001`–`007`, `06-materiality-and-depth`, `EVAL-007`)

- [ ] **`CHK-VIS-001` (4-Layer 경계 준수 — `VIS-RULE-001`):**
  - Layer 0 (`canvas`): `Solid` (`AuraScaffold`, `AuraAdaptiveTwoPane`, `FloatingGlassScaffold` 스크롤 캔버스)
  - Layer 1 (`content`): `Solid` (`AuraCard` `Outlined`/`Filled`/`Elevated`, `AuraTextField`, `AuraButton` 등 — 스크롤 목록/폼에 `AuraCardVariant.Glass` 및 `GlassSurface` 사용 금지)
  - Layer 2 (`chrome`): `Solid` / `Glass` / `LiquidGlass` (`AuraTopAppBar`, `FloatingTopPillBar`, `FloatingBottomIsland`, `AuraSearchBar`, `AuraFab`, `AuraSnackbar`)
  - Layer 3 (`spatial`): `Solid` / `Glass` / `LiquidGlass` (`AuraDialog`, `MorphingMorePopover`)
- [ ] **`CHK-VIS-002` (Blur & Hero 예산 — `VIS-RULE-002`, `VIS-RULE-006`):** 한 뷰포트 내 동시 live-blur(`Glass` / `LiquidGlass`) 패널은 **`<= 3`개**, 화면당 `LiquidGlass` focal hero는 **`<= 1`개**로 제한한다.
- [ ] **`CHK-VIS-003` (Glass 대비 & Edge Scrim — `VIS-RULE-003`):** 플로팅 유리 크롬 아래로 콘텐츠가 지나갈 때 `FloatingGlassScaffold`의 Layer 1 점진적 edge scrim을 통해 본문/라벨 대비 `>= 4.5:1`, 아이콘/테두리 대비 `>= 3:1`을 유지한다.
- [ ] **`CHK-VIS-004` (하드웨어·접근성 폴백 — `VIS-RULE-004`):** `AuraTheme.reduceTransparency`(Reduced Transparency, 절전 모드, 고대비 텍스트) 또는 API < 31 환경에서 수동 `Build.VERSION` 분기 없이 `AuraTheme.effectiveMaterial(...)` 경로를 통해 `Solid`로 폴백한다.

### 1.3 Dual-Track Motion 규율 (`MOT-RULE-001`–`007`, `03-motion-interaction`, `07-interaction-verification`, `EVAL-004`, `EVAL-008`)

- [ ] **`CHK-MOT-001` (Tokens Only — `MOT-RULE-002`):** 프로덕션 UI 코드에 숫자 리터럴 `tween(300)` / `spring(dampingRatio = ..., stiffness = ...)` 및 기본값으로 `tween(300)`을 암묵 사용하는 `Crossfade()`(스펙 미지정)가 없다. 모든 커스텀 애니메이션은 `AuraTheme.motion.*` (`pressInSpring()`, `pressOutSpring()`, `spatialSpring()`, `elasticStretchSpring()`, `enterSpec()`, `exitSpec()`, `stateChangeDurationMillis`, `staggerDelay(index)`)를 사용한다.
- [ ] **`CHK-MOT-002` (Dual-Track 분리 — `MOT-RULE-003`):** 루트는 `AuraMotionProfile.Standard`(0–3% 오버슈트)를 유지하고, 화면당 최대 1개의 focal spatial chrome/modal(`FloatingBottomIsland`, `AuraDialog`, `AuraFab`)만 중첩 `AuraTheme(motionProfile = AuraMotionProfile.Expressive)`를 적용한다.
- [ ] **`CHK-MOT-003` (타이밍 비대칭 & 인터럽트 연속성 — `MOT-RULE-004`, `MOT-RULE-005`):** 입장 > 퇴장(`enterSpec` > `exitSpec`), 눌림 < 복귀(`pressInDurationMillis` < `pressOutDurationMillis`) 및 스프링 기반 연속 전환을 유지한다.
- [ ] **`CHK-MOT-004` (Reduced Motion — `MOT-RULE-007`):** 커스텀 애니메이션 및 화면 전환이 `AuraTheme.motion.reduceMotion` / `rememberAuraReducedMotion()`을 확인해 공간 이동(scale/translation/stagger)은 `snap()`으로 즉시 확정하고 투명도/색상/로딩 시맨틱(`stateDescription`)은 유지한다.

### 1.4 반응형 & 접근성 공통 게이트 (`EVAL-003`, `EVAL-005`, `05-responsive-layout`)

- [ ] **`CHK-RSP-001` (2축 레이아웃 토큰 — `05-responsive-layout`):** 화면 바깥 여백, 섹션 간격, 그리드 열 수, 최대 콘텐츠 폭, 2단 전환 여부를 임의 `widthDp > 600` 분기나 하드코딩 dp 대신 `AuraTheme.layout` (`screenMarginHorizontal`, `screenMarginVertical`, `sectionSpacing`, `gridColumns`, `gridGutter`, `maxContentWidth`, `supportsTwoPane`, `primaryPaneFraction`)으로 제어한다.
- [ ] **`CHK-RSP-002` (5개 프로필 검증):** `ExtraSmall` (`< 360dp`), `Compact` (`360–599dp`), `Medium · Square` (`~1:1` 폴더블), `UltraWide` (가로모드 전화 `h < 480dp`), `Expanded` (`>= 840dp` 태블릿) 모두에서 텍스트 잘림·이중 패딩·과도한 가로 늘어짐이 없다.
- [ ] **`CHK-A11Y-001` (터치 타깃 & 시맨틱):** 모든 인터랙티브 컨트롤은 최소 `48.dp × 48.dp` 터치 영역을 확보하고, 제목/역할/상태(`heading`, `Role.*`, `stateDescription`, `error`, `liveRegion`) 시맨틱을 유지한다.

---

## 2. Tier B — 24개 Component Specs 도출 체크리스트

> 모든 항목은 `aura-design-system/docs/components/`의 해당 컴포넌트 스펙(`Rule ID`, `Do / Don't`, `State Precedence`, `Material/Layer`, `Motion Track`, `Responsive`, `Accessibility`)에서 직접 도출했다.

### 2.1 Actions (`00-button-principles.md`)

| ID | 대상 컴포넌트 | 스펙 근거 | 체크 항목 | `drink-library` 적용 상태 |
|---|---|---|---|---|
| `CHK-ACT-001` | `AuraButton` | `BTN-RULE-001` | 한 화면 또는 모달 다이얼로그당 **`Primary` 버튼은 최대 1개**만 사용한다. | 사용 중 |
| `CHK-ACT-002` | `AuraButton` | `BTN-RULE-002` | 반복 카드·리스트 행 내부에 `Primary` 또는 `Large fullWidth` CTA를 두지 않고 `Secondary`/`Outline`/`Ghost`를 사용한다. | 사용 중 |
| `CHK-ACT-003` | `AuraButton` | `BTN-RULE-003` | 되돌릴 수 없는 삭제/초기화의 **진입 화면 트리거**는 `Outline` 또는 `Ghost`를 사용하고, solid **`Destructive`는 다이얼로그 등 최종 확인 단계에만** 사용한다. | 사용 중 (`RecordDetailScreen` 점검 필요) |
| `CHK-ACT-004` | `AuraButton` | `BTN-RULE-005`, `BTN-001` | `Small(36dp)` 포함 외부 높이를 강제 축소(`Modifier.height(<48.dp)`)하지 않고 `48.dp` 터치 타깃을 보존한다. | 사용 중 |
| `CHK-ACT-005` | `AuraButton` | `BTN-RULE-006`, §4 | 비동기 저장 중 `loading = true`를 전달해 너비 점프 없이 스피너를 표시하고 중복 탭(`debounceIntervalMs`)을 방지한다. `text`는 비어있지 않은 명확한 동사를 사용한다 (`BTN-002`). | 사용 중 |
| `CHK-ACT-006` | `AuraButton` | §7 Responsive | `Medium·Square`/`Expanded`에서 `fullWidth` CTA가 700dp+ 전체 폭으로 과도하게 늘어나지 않도록 `maxContentWidth`/pane 폭으로 제한한다. | 사용 중 |
| `CHK-ACT-007` | `AuraIconButton` | `IBTN-RULE-001` | 모든 독립 `AuraIconButton`은 명확한 동작 동사 `contentDescription`을 제공한다 (`null` 금지). | 사용 중 |
| `CHK-ACT-008` | `AuraIconButton` | `IBTN-RULE-002`, `003`, `004` | 앱바는 `Ghost`, 미디어 위는 `Floating`을 사용하며, 토글 상태가 있으면 색상만 바꾸지 않고 `selected: Boolean?`을 전달한다. `48.dp` 히트 영역을 자르지 않는다. | 사용 중 |
| `CHK-ACT-009` | `AuraFab` | `FAB-RULE-001`~`004` | 화면당 최대 1개, 생성 작업 전용, 스크롤 축소(`expanded = false`) 시에도 `contentDescription` 필수. *(현재 `FloatingBottomIsland` Split Action으로 대체 — 도입 시 적용)* | 미사용 |
| `CHK-ACT-010` | `AuraSegmentedButton` | `SEG-RULE-001`~`004` | **2–5개** 상호배타적 즉시 화면 전환(저장 버튼 불필요)에만 사용하며, 각 옵션은 짧은 1단어 라벨로 구성한다. 최상위 앱 탭이나 다중 선택에 사용하지 않는다. | 사용 중 |
| `CHK-ACT-011` | `AuraSegmentedButton` | §5 Responsive | `Compact`는 `fullWidth = true`, `Medium·Square`/`Expanded`에서는 과도한 폭 확장을 피하도록 bounded width 또는 `fullWidth = false`를 고려한다. | 사용 중 |
| `CHK-ACT-012` | `AuraChipButton` | `CHIP-RULE-001`, `002` | **`variant`를 목적에 맞게 분리**한다: 목록 필터링은 `Filter`, 테이스팅/음용 속성 태그는 **`Tag`**, 문맥 단축키는 `Assist`, 미디어/플로팅 바 위는 `Glass`. 라벨은 1줄(`1–3`단어)로 유지하고 문자열 체크마크(`"✓"`, `"★"`) 대신 `leadingIcon`/`selected` 시각 상태를 활용한다. | 사용 중 (`TagPicker`/`TasteEditor`/`RecordDetailStep`/`CollectionScreen` 점검 필요) |
| `CHK-ACT-013` | `AuraChipButton` | `CHIP-RULE-003`, `004` | 유한 옵션(3–8개)은 `FlowRow`, 다수 옵션은 `LazyRow`로 배치하고 `48.dp` 터치 높이를 유지한다. | 사용 중 |

### 2.2 Inputs (`00-input-principles.md`)

| ID | 대상 컴포넌트 | 스펙 근거 | 체크 항목 | `drink-library` 적용 상태 |
|---|---|---|---|---|
| `CHK-INP-001` | `AuraTextField` | `INP-RULE-001` | 구조화 메타데이터는 `Outlined`/`Filled`, 장문 작성(메모/테이스팅 노트)은 **`Paperless` (`singleLine = false`, `minLines >= 3`)**를 사용한다. | 사용 중 |
| `CHK-INP-002` | `AuraTextField` | `INP-RULE-004`, `005` | 모든 입력 필드에 **영속적 `label`**을 제공한다(`placeholder`로 `label` 대체 금지). 오류 시 색상만 바꾸지 않고 `errorText`를 전달한다 (`TF-003`). | 사용 중 |
| `CHK-INP-003` | `AuraNumberField` | `NUM-RULE-001`~`003`, `INP-RULE-003` | 숫자·금액 입력은 `AuraTextField` 대신 `AuraNumberField`를 쓰고 단위는 `prefixText`/`suffixText`에 명시한다(`placeholder` 내 암묵 표기 금지). `showSteppers = true`일 때 `minValue`/`maxValue` 범위를 지정한다. | 사용 중 |
| `CHK-INP-004` | `AuraSearchBar` | `SRCH-RULE-001`~`003` | `onQueryChange` 실시간 필터링과 **`onSearch` (`ImeAction.Search`)**를 함께 연결한다. 일반 리스트/콘텐츠 위에서는 `AuraSearchBarSurface.Solid`를 사용하고, 숨겨진 필터가 있으면 `activeFilterCount`와 `onFilterClick`을 노출한다. | 사용 중 (`onSearch` 연결 점검 필요) |
| `CHK-INP-005` | `AuraRatingBar` | `RAT-RULE-001`~`003` | 평가 입력은 `allowHalfRating = true`와 `label`을 제공하고, 읽기 전용 표시는 `onRatingChange = null` + `Small`로 구분한다. 인터랙티브 행은 `48.dp` 이상 높이를 유지한다. | 사용 중 |
| `CHK-INP-006` | `AuraDateTimePicker` | `DTP-RULE-001`~`003` | 달력/시간 선택 시 `Date`/`DateTime` 사용, 고정 빈티지 피커 제공 시 `YearVintage`와 `minYear..maxYear` 클램핑을 적용한다. *(현재 빈티지는 선택 자유 입력이라 `AuraNumberField` 사용 — `YearVintage` 피커 추가 시 적용)* | 미사용 |
| `CHK-INP-007` | `AuraSwitch` | `SW-RULE-001`~`003`, `INP-RULE-002` | 저장 버튼 없이 **즉시 적용**되는 설정에만 사용하고 긍정문 `label`을 쓴다. | 미사용 |
| `CHK-INP-008` | `AuraCheckbox` | `CB-RULE-001`~`003`, `INP-RULE-002` | 다중 선택·체크리스트·폼 제출 전 동의에 사용한다 (즉시 토글 설정에는 사용 금지). | 미사용 |
| `CHK-INP-009` | `AuraRadio` | `RD-RULE-001`~`003` | 2–5개 설명형 단일 선택 시 `Modifier.selectableGroup()`으로 묶어 사용한다 (단독 On/Off 체크박스 대체 금지). | 미사용 |
| `CHK-INP-010` | `AuraSlider` | `SLD-RULE-001`~`003` | 연속/단계 범위 조절 시 `label` + `valueLabel`을 제공하고, 무거운 저장은 `onValueChangeFinished`에 연결한다. | 미사용 |

### 2.3 Containment (`00-containment-principles.md`)

| ID | 대상 컴포넌트 | 스펙 근거 | 체크 항목 | `drink-library` 적용 상태 |
|---|---|---|---|---|
| `CHK-CNT-001` | `AuraCard` | `CARD-RULE-001`, `VIS-RULE-001`, `CNT-RULE-003` | 리스트·폼·피드 카드는 `Outlined`/`Filled`/`Elevated`만 사용한다 (`AuraCardVariant.Glass`는 홈 런처 배경 위젯 전용이므로 일반 목록·콘텐츠에 금지). | 사용 중 |
| `CHK-CNT-002` | `AuraCard` | `CARD-RULE-002`, `CNT-RULE-002` | 전체 카드가 액션일 때만 `onClick != null`을 전달하고, 클릭 카드 내부에 또 다른 클릭 카드 중첩 및 `Modifier.clickable` 우회를 금지한다. | 사용 중 |
| `CHK-CNT-003` | `AuraCard` | `CARD-RULE-003` Do/Don't | 내부 여백은 **`AuraCardPadding` (`None`/`Compact`/`Medium`/`Large`)**으로 제어하고, `Medium(16.dp)` 기본 패딩 위에 직계 자식 `Column`/`Row`의 `.padding(16.dp)`을 **이중 중첩하지 않는다**. | 사용 중 (`CollectionScreen` 이중 패딩 점검 필요) |
| `CHK-CNT-004` | `AuraCard` | §2 Do/Don't, `CNT-RULE-004` | `selected = true`로 테두리/색상 동시 변화를 표시하고, 장문 에디터 페이지 전체를 하나의 카드로 감싸지 않는다. | 사용 중 |
| `CHK-CNT-005` | `AuraDialog` | `DLG-RULE-001`~`003`, §4–§5 | **`if (show) { AuraDialog(visible = true) }` 조건부 래핑 대신 `AuraDialog(visible = show, ...)`를 직접 전달**해 상태 우선순위(`Hidden`)와 `Expressive` 모션 진입 계약이 정상 동작하게 한다. 파괴적 확인은 `AuraDialogVariant.Destructive` + 구체적 동사 `confirmText`, 버튼은 최대 2개, 메시지 1–3줄, 탈출 경로(`dismissText`/`dismissOnClickOutside`/Back)를 보장한다. | 사용 중 (`RecordDetailScreen`, `RecordFlow`, `ShareCardDialog` 점검 필요) |
| `CHK-CNT-006` | `AuraDivider` | `DIV-RULE-001`~`002` | 기본 `Subtle` 톤과 `1.dp` 두께를 유지하며 `Box(Modifier.height(1.dp).background(...))` 직접 구현을 금지한다. | 사용 중 |

### 2.4 Feedback (`00-feedback-principles.md`)

| ID | 대상 컴포넌트 | 스펙 근거 | 체크 항목 | `drink-library` 적용 상태 |
|---|---|---|---|---|
| `CHK-FBK-001` | `AuraBadge` / `AuraBadgedBox` | `FBK-RULE-001`~`003`, `BDG-RULE-001`~`003` | **`Success`/`Warning`/`Error`/`Info`는 의미적 상태(성공·경고·오류·정보)에만 사용**하고, 비상태 분류(예: 와인/위스키 주종 구분)에는 `Brand`/`Neutral`을 사용한다. 읽기 전용이며 `Modifier.clickable`을 붙이지 않는다. 라벨은 1–2단어, 숫자 카운터는 `count`/`maxCount`(`99+`)를 사용한다. | 사용 중 (`DDBadges`, `LookupScreen` 수정 대상) |
| `CHK-FBK-002` | `AuraLinearProgressBar` / `AuraStepProgressBar` | `PRG-RULE-001`~`002` | `0f..1f` / `0..totalSteps` 범위 클램핑과 함께 수치 설명 텍스트 및 `contentDescription`을 제공한다. 2–8단계는 `AuraStepProgressBar`, 그 이상·연속값은 `AuraLinearProgressBar`를 사용한다. | 사용 중 |
| `CHK-FBK-003` | `AuraSnackbar` / `AuraSnackbarHost` | `SNK-RULE-001`~`003` | `AuraScaffold`/`FloatingGlassScaffold`의 `snackbarHost` 슬롯에 전달해 하단 Island/시스템 바 회피를 자동화한다(임의 `92.dp` 오프셋 금지). 메시지는 1–3줄, 액션 최대 1개, 오류는 `AuraSnackbarVariant.Error`를 고려한다. | 사용 중 |

### 2.5 Layout & Navigation (`00-navigation-principles.md`, `05-responsive-layout.md`)

| ID | 대상 컴포넌트 | 스펙 근거 | 체크 항목 | `drink-library` 적용 상태 |
|---|---|---|---|---|
| `CHK-LAY-001` | `AuraScaffold` / `FloatingGlassScaffold` | `LAY-RULE-001`, `002`, `SCF-001`, `002` | 화면당 Scaffold는 1개만 사용(중첩 금지)하고, `content(innerPadding)`으로 받은 `PaddingValues`를 스크롤/본문 컨테이너에 정확히 전달해 시스템 바 및 플로팅 크롬 가림을 방지한다. | 사용 중 |
| `CHK-LAY-002` | `AuraAdaptiveTwoPane` | `LAY-RULE-003`, `004`, `PAN-001`, `002` | `primaryPane`에 요약/컨트롤/마스터, `secondaryPane`에 상세/추천/에디터를 배치한다. **외부 `.verticalScroll()` 래핑을 금지**하고, 각 pane이 독립 스크롤을 유지하도록 `FloatingGlassScaffold` 사용 시 `contentPadding`을 바깥 `Box`가 아닌 각 pane 내부 여백으로 적용한다. | 사용 중 (`DDTwoPaneContent`, `DrinkPicker` 점검 필요) |
| `CHK-LAY-003` | `AuraAdaptiveGrid` | `05-responsive-layout` | 균등 우선순위 카드 그리드에 `AuraTheme.layout.gridColumns` / `gridGutter`를 연결한다. | 사용 중 |
| `CHK-NAV-001` | `AuraTopAppBar` / `FloatingTopPillBar` | `NAV-RULE-001`, `TBAR-RULE-001` | **최상위(`TopLevel`) 및 상세(`Detail`) 화면은 `FloatingCapsule` 또는 `FloatingSplit`(`FloatingGlassScaffold`)을 사용**하고, **`SurfaceDocked`는 고밀도 폼·에디터(`Editor`)에만** 제한한다. | 사용 중 (`DDScreenScaffold` `Detail` 수정 대상) |
| `CHK-NAV-002` | `AuraTopAppBar` | `NAV-RULE-002`, `003`, `005`, `TBAR-RULE-002`~`005` | 좌측 네비게이션 1개 + 우측 액션 `2–3`개 이하(`AuraIconButton`/`AuraChipButton`만 허용). 플로팅 바 사용 시 실제 스크롤 상태(`rememberFloatingScrollProgress` 등)를 `scrollFraction`에 연결한다. | 사용 중 (`scrollFraction` 미연결 수정 대상) |
| `CHK-NAV-003` | `AuraBottomIsland` / `FloatingBottomIsland` | `BISL-RULE-001`~`004`, `MOT-RULE-003` | 최상위 탭 2–5개를 호스트하고 `FloatingGlassScaffold`와 결합해 하단 패딩을 확보한다. **단일 `Expressive` 모션 트랙(`AuraTheme(motionProfile = AuraMotionProfile.Expressive)`)**으로 감싸고, `splitActionLabel`과 각 탭의 아이콘/라벨 접근성을 제공한다. Deprecated `GlassStyle` 대신 권장 패턴(`AuraMaterial` 또는 호환 주의)을 점검한다. | 사용 중 (`Expressive` 래핑 누락 수정 대상) |

---

## 3. Tier C — `drink-library` 화면 & 로컬 컴포넌트 검증 매트릭스

| 대상 파일 / 화면 | 적용 Aura 요소 | 집중 점검 체크리스트 ID |
|---|---|---|
| `ui/theme/Theme.kt` | `AuraTheme` | `CHK-SYS-005`, `CHK-MOT-002` |
| `ui/navigation/DDScreenScaffold.kt` | `FloatingGlassScaffold`, `AuraScaffold`, `AuraTopAppBar`, `FloatingBottomIsland`, `AuraIconButton` | `CHK-NAV-001`, `CHK-NAV-002`, `CHK-NAV-003`, `CHK-VIS-002`, `CHK-MOT-002` |
| `ui/navigation/DrinkDiaryApp.kt` | `AuraSnackbarHost`, `rememberAuraSnackbarHostState`, `AuraIconButton`, 화면 라우팅 | `CHK-NAV-002`, `CHK-FBK-003`, `CHK-ACT-007` |
| `ui/component/DDResponsive.kt` | `DDCenteredContent`, `DDTwoPaneContent`, `AuraAdaptiveTwoPane` | `CHK-LAY-001`, `CHK-LAY-002`, `CHK-RSP-001`, `CHK-RSP-002` |
| `ui/component/DDBadges.kt` | `AuraBadge` | `CHK-FBK-001` |
| `ui/component/DDCards.kt` | `AuraCard`, `AuraBadge`, `AuraLinearProgressBar`, `AuraText` | `CHK-CNT-001`~`003`, `CHK-FBK-001`, `CHK-FBK-002` |
| `ui/component/DDTasteComponents.kt` | `AuraCard`, `AuraBadge`, `AuraText`, 커스텀 스펙트럼 바 | `CHK-SYS-002`~`004`, `CHK-MOT-004`, `CHK-CNT-001`~`003` |
| `ui/component/ProbeComponents.kt` | `AuraCard`, `AuraText`, `AuraTheme.layout` | `CHK-CNT-001`~`004`, `CHK-A11Y-001` |
| `ui/component/DDUriImage.kt` | `AuraText`, `AuraTheme.colors` | `CHK-SYS-002`, `CHK-SYS-003` |
| `ui/profile/ProfileScreen.kt` | `AuraSegmentedButton`, `AuraButton`, `AuraDivider`, `DDCenteredContent`/`DDTwoPaneContent` | `CHK-ACT-010`, `011`, `CHK-LAY-002`, `CHK-NAV-002` |
| `ui/collection/CollectionScreen.kt` | `AuraSearchBar`, `AuraSegmentedButton`, `AuraChipButton`, `AuraCard`, `LazyVerticalGrid` | `CHK-CNT-003`, `CHK-INP-004`, `CHK-ACT-012`, `CHK-NAV-002` |
| `ui/collection/RecordDetailScreen.kt` | `AuraDialog`, `AuraButton`, `AuraRatingBar`, `AuraBadge`, `AuraDivider`, `DDTwoPaneContent` | `CHK-ACT-003`, `CHK-CNT-005`, `CHK-INP-005`, `CHK-NAV-001`, `CHK-NAV-002` |
| `ui/lookup/LookupScreen.kt` | `AuraSearchBar`, `AuraCard`, `AuraBadge`, `AuraDivider`, `DDCenteredContent` | `CHK-INP-004`, `CHK-FBK-001`, `CHK-NAV-002` |
| `ui/record/DrinkPicker.kt` | `AuraAdaptiveGrid`, `AuraCard`, `DDTwoPaneContent` | `CHK-LAY-002`, `CHK-LAY-003`, `CHK-CNT-003` |
| `ui/record/ProbeSequenceScreen.kt` | `AuraStepProgressBar`, `AuraButton`, `Crossfade`, `DDProbeQuestion` | `CHK-FBK-002`, `CHK-MOT-001`, `CHK-MOT-004` |
| `ui/record/RecordDetailStep.kt` | `AuraTextField`, `AuraNumberField`, `AuraRatingBar`, `AuraSegmentedButton`, `AuraChipButton`, `AuraButton` | `CHK-ACT-005`, `CHK-ACT-012`, `CHK-INP-001`~`003`, `CHK-INP-005` |
| `ui/record/RecordFlow.kt` | `Crossfade`, `AuraDialog`, `AuraButton` | `CHK-ACT-001`, `CHK-CNT-005`, `CHK-MOT-001`, `CHK-MOT-004` |
| `ui/record/TagPicker.kt` & `TasteEditor.kt` | `AuraChipButton`, `FlowRow` | `CHK-ACT-012`, `CHK-ACT-013` |
| `ui/settings/SettingsScreen.kt` | `AuraCard`, `AuraButton`, `AuraDivider` | `CHK-ACT-001`, `CHK-NAV-001`, `CHK-NAV-002` |
| `ui/share/ShareCardDialog.kt` & `ShareCardRenderer.kt` | `AuraDialog`, `AuraColors` | `CHK-CNT-005`, `CHK-SYS-002`, `CHK-RSP-002` |
