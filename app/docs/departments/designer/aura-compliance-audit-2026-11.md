# Aura Design System 준수 감사 리포트 (`drink-library`, 2026-11-16)

- **기준 체크리스트:** [aura-compliance-checklist.md](./aura-compliance-checklist.md)
- **기준 AAR:** `app/libs/aura-design-system-1.1.0.aar`
- **점검 범위:** `app/src/main/java/com/bluemarlin/drinkdiary/ui/**` (전 화면 8개 + 로컬 컴포넌트 9종), `MainActivity.kt`, `app/build.gradle.kts`, `app/src/test/**`
- **사용자 방침:** 예외(`WAIVED`) 없이 Aura Design System 스펙 원안(`FBK-RULE-001`, `NAV-RULE-001` 등)을 준수하도록 수정하며, UI 적합성 테스트(`AuraConformanceTest`)까지 포함한다.

---

## 1. 초기(Baseline) 감사 요약

기존 정적 스크립트(`verify-aura-app.ps1`) 실행 결과는 `Errors: 0, Warnings: 0`이었으나, 컴포넌트 스펙 계약(`aura-compliance-checklist.md`) 대조 결과 **총 11건의 스펙 위반/미흡(`FAIL`)**을 확인했다.

| 우선순위 | 체크리스트 ID | 대상 파일 | 발견된 문제 | 개선 조치 |
|---|---|---|---|---|
| **P0** | `CHK-NAV-001` (`NAV-RULE-001`) | `ui/navigation/DDScreenScaffold.kt` | `DDScreenType.Detail`(`RecordDetailScreen`, `SettingsScreen`)이 `SurfaceDocked`를 사용하여 상세 화면 플로팅 원칙 위반 | `Detail`을 `FloatingGlassScaffold(hasBottomIsland = false)` + `AuraTopAppBar(FloatingCapsule, TranslucentGlass)`로 전환, `SurfaceDocked`는 `Editor` 전용으로 제한 |
| **P0** | `CHK-NAV-003` (`MOT-RULE-003`), `CHK-SYS-005` | `ui/navigation/DDScreenScaffold.kt`, `ui/theme/Theme.kt` | `FloatingBottomIsland`가 루트 `Standard` 모션 트랙에서 실행(`Expressive` 래핑 누락). `Theme.kt`에 `material`/`motionProfile` 명시 누락 | `Theme.kt`에 `AuraMaterial.Solid`, `AuraMotionProfile.Standard` 명시 및 `FloatingBottomIsland`를 `AuraTheme(motionProfile = AuraMotionProfile.Expressive)`로 래핑 |
| **P0** | `CHK-NAV-002` (`NAV-RULE-005`, `TBAR-RULE-003`) | `ui/navigation/DrinkDiaryApp.kt`, `ProfileScreen.kt`, `CollectionScreen.kt`, `LookupScreen.kt`, `RecordDetailScreen.kt`, `SettingsScreen.kt` | `DDScreenScaffold` `scrollFraction`이 전 화면에서 `0f`로 고정되어 스크롤 연동 블러/고도/백드롭 무효화가 작동하지 않음 | `ScrollState` (`rememberFloatingScrollProgress`) 및 `LazyGridState` 스크롤 진행도를 각 화면에서 상위로 보고해 `scrollFraction` 연결 |
| **P0** | `CHK-LAY-002` (`LAY-RULE-001`, `BISL-RULE-002`, `aura-adaptive-two-pane.md`) | `ui/component/DDResponsive.kt`, `ProfileScreen.kt`, `DrinkPicker.kt`, `RecordDetailScreen.kt` | `DDTwoPaneContent`가 `Box.padding(contentPadding)`으로 바깥에서 패딩을 잘라 플로팅 크롬 뒤로 스크롤이 흐르지 않음. `DrinkPicker` Two-Pane 상단 제목에 `contentPadding` 전체(bottom 포함) 적용 | `DDTwoPaneContent`가 `contentPadding`을 각 pane 내부 상하 여백으로 전달하도록 개선, `DrinkPicker`는 top padding만 적용 |
| **P0** | `CHK-CNT-005` (`aura-dialog.md` §4–§5) | `RecordDetailScreen.kt`, `RecordFlow.kt`, `ShareCardDialog.kt` | `if (confirming) AuraDialog(visible = true, ...)` 형태로 조건부 마운트하여 `AuraDialog` `visible` 계약 우회 | `AuraDialog(visible = show, ...)`를 직접 전달해 `Hidden` 상태 우선순위 및 `Expressive` 진입 계약 준수 |
| **P1** | `CHK-FBK-001` (`FBK-RULE-001`, `aura-badge.md`) | `ui/component/DDBadges.kt`, `ui/lookup/LookupScreen.kt` | 비상태 분류(와인/위스키 주종)에 `Error`/`Warning` 상태 배지를 오용하고 있음. `Repurchase`는 `Brand` 대신 의미 상태 색 가능 | `DDDrinkBadge`를 `Wine -> AuraBadgeVariant.Brand`, `Whiskey -> AuraBadgeVariant.Neutral`로 수정; `LookupRecordCard`의 `Repurchase -> AuraBadgeVariant.Success` 적용 |
| **P1** | `CHK-ACT-003` (`BTN-RULE-003`) | `ui/collection/RecordDetailScreen.kt` | 1차 삭제 진입 버튼(`"이 기록 지우기"`)에 최종 확인 전용인 `AuraButtonVariant.Destructive` 사용 | 진입 버튼은 `AuraButtonVariant.Outline`으로 변경하고, 최종 확인 `AuraDialog(Destructive)` 내부 `"지우기"`만 Destructive 유지 |
| **P1** | `CHK-CNT-003` (`CARD-RULE-003`) | `ui/collection/CollectionScreen.kt` | `RecordRow`의 `AuraCard` 기본 `Medium(16.dp)` 패딩 위에 내부 `Row.padding(16.dp)`가 이중 적용됨 (`32.dp`) | `AuraCard(padding = AuraCardPadding.Medium)` 유지하고 내부 `Row`의 중복 `padding(16.dp)` 제거 |
| **P1** | `CHK-ACT-012` (`CHIP-RULE-001`, `002`) | `TagPicker.kt`, `TasteEditor.kt`, `RecordDetailStep.kt`, `CollectionScreen.kt` | 속성/테이스팅 칩에 `AuraChipVariant.Tag` 대신 기본 `Filter` 사용. `CollectionScreen` 필터 칩 라벨에 문자열 `"★"`, `"✓"` 하드코딩 | `TagPicker`/`TasteEditor`/`RecordDetailStep` 칩에 `AuraChipVariant.Tag` 적용, `CollectionScreen`은 깔끔한 텍스트 라벨 + `selected` 상태로 정돈 |
| **P1** | `CHK-MOT-001`, `CHK-MOT-004` (`MOT-RULE-002`, `007`) | `RecordFlow.kt`, `ProbeSequenceScreen.kt` | `Crossfade`에 `animationSpec` 미지정(Compose 기본 `tween(300)` 및 `reduceMotion` 미반영) | `AuraTheme.motion.reduceMotion` 시 `snap()`, 일반 시 `AuraTheme.motion.enterSpec()` 지정 |
| **P1** | `CHK-INP-004` (`SRCH-RULE-001`) | `CollectionScreen.kt`, `LookupScreen.kt` | `AuraSearchBar`에 `onSearch` 콜백(`ImeAction.Search`) 미연결 | `LocalFocusManager.current.clearFocus()` / `LocalSoftwareKeyboardController.current?.hide()` 연결 |

---

## 2. Pass / N/A 항목 확인

- **`CHK-SYS-001`~`004` (`EVAL-002`):** `androidx.compose.material3` 0건, 프로덕션 `Color(0x...)` 0건, `.sp` 0건, `AuraTheme.shapes` 사용 확인 (`PASS`).
- **`CHK-VIS-001`, `002`:** Layer 1 리스트/폼에 `AuraCardVariant.Glass` 미사용, 뷰포트당 live blur `<= 2` (`AuraTopAppBar` + `FloatingBottomIsland`, `LiquidGlass` hero `<= 1`) (`PASS`).
- **`CHK-INP-001`~`003`, `005`:** `AuraTextField` 영속 라벨 + 장문 메모 `Paperless`, `AuraNumberField` `suffixText = "원"`, `AuraRatingBar(allowHalfRating = true)` 및 읽기 전용(`onRatingChange = null`, `Small`) 구분 정상 (`PASS`).
- **`CHK-CNT-006`, `CHK-FBK-002`, `CHK-FBK-003`:** `AuraDivider`, `AuraLinearProgressBar`, `AuraStepProgressBar`, `AuraSnackbarHost` 모두 Aura 1.1.0 API 적용 (`PASS`).
- **미사용 컴포넌트 (`CHK-ACT-009`, `CHK-INP-006`~`010`):** `AuraFab`, `AuraDateTimePicker`, `AuraSwitch`, `AuraCheckbox`, `AuraRadio`, `AuraSlider`는 현재 앱 UI에 해당 요구사항 없음 (`N/A`).

---

## 3. 개선 및 검증 계획 상태

- [ ] Phase 3 코드 수정 (P0 5건 + P1 6건)
- [ ] 스펙 기반 정적 검증 스크립트(`scripts/verify-aura-specs.ps1`) 추가
- [ ] Robolectric + Compose UI 적합성 테스트(`AuraConformanceTest.kt`) 작성 및 통과
- [ ] `design-system.md` / `aura-adoption.md` / `task-log.md` 업데이트 및 전체 게이트(`testDebugUnitTest`, `ktlintCheck`, `lint`, `verify-aura-app.ps1`) 통과
