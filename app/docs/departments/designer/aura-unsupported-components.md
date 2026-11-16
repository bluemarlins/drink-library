# Aura Design System 미지원 컴포넌트 목록 (Unsupported / Gap Components)

- **기준 AAR:** `app/libs/aura-design-system-1.1.0.aar` (`bluemarlins/aura-design-system` `v1.1.0`, package `com.example.ds`)
- **최종 갱신일:** 2026-11-16
- **관련 문서:** [design-system.md](../../../specs/designer/design-system.md), [aura-adoption.md](./aura-adoption.md)

---

## 1. Aura 1.1.0에 추가되어 해소된 항목 (Resolved)

`v1.0.0` 적용 당시 미지원이었던 아래 5개 항목은 `v1.1.0` 편입으로 로컬 Gap 구현을 제거하고 Aura API로 전환했습니다.

| # | 기존 Gap 항목 | 대체된 Aura 1.1.0 API | 비고 |
| --- | --- | --- | --- |
| 1 | `DDSnackbarHost`, `DDSnackbarState` | `AuraSnackbarHost`, `rememberAuraSnackbarHostState`, `AuraScaffold(snackbarHost = ...)`, `FloatingGlassScaffold(snackbarHost = ...)` | `DDSnackbar.kt` 삭제, 하단 Island 회피 자동 처리 |
| 2 | `DDProgressBar` | `AuraLinearProgressBar` | `DDCards.kt` 내 `DDProfileProgressCard` 교체 |
| 3 | `DDProbeProgress` | `AuraStepProgressBar` | `ProbeSequenceScreen.kt` 교체 |
| 4 | `DDDivider` | `AuraDivider` | 5개 화면 전체 교체 |
| 5 | `DDShape` | `AuraTheme.shapes` (`extraSmall`, `small`, `medium`, `large`, `extraLarge`, `full`) | `DDShape.kt` 삭제 |

---

## 2. Aura 1.1.0에서도 로컬 유지하는 항목

| # | 항목 / 용도 | 앱 로컬 구현 | 유지 사유 | 적용 방식 및 위치 |
| --- | --- | --- | --- | --- |
| 1 | **Local URI Image Loader** (Content URI 비동기 이미지 디코더) | `DDUriImage` | Android `ContentResolver`/`ImageDecoder` 기반 이미지 로더는 디자인 시스템 범위 외 | [`DDUriImage.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDUriImage.kt) — 로컬 비동기 디코더 유지 (`AuraTheme.shapes`와 조합 사용). |

---

## 3. 도메인 복합 컴포넌트 (Aura 컴포넌트 조합으로 유지)

단일 UI 프리미티브가 아닌 DrinkDiary 도메인 전용 조합 UI로, 내부는 모두 Aura 컴포넌트와 토큰을 사용합니다.

| 항목 | 내부 사용 Aura 요소 | 위치 |
| --- | --- | --- |
| `DDDrinkBadge` | `AuraBadge` (`Error` = 와인, `Warning` = 위스키) | [`DDBadges.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDBadges.kt) |
| `DDRepurchaseBadge` | `AuraBadge` (`Brand`) | [`DDBadges.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDBadges.kt) |
| `DDTasteTypeBadge` | `AuraText(displayMedium)`, `AuraBadge(Neutral)` | [`DDCards.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDCards.kt) |
| `DDTasteSentenceCard` | `AuraCard(Outlined)`, `AuraText(headlineMedium, bodyMedium)` | [`DDCards.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDCards.kt) |
| `DDProfileProgressCard` | `AuraCard(Outlined)`, `AuraText`, `AuraLinearProgressBar` | [`DDCards.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDCards.kt) |
| `DDProbeQuestion` | `AuraCard(Outlined)`, `AuraText`, `AuraTheme.layout` | [`ProbeComponents.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/ProbeComponents.kt) |

---

## 4. 내비게이션 구조 참고 사항

- **NavigationRail / NavigationDrawer 미제공:** Aura `1.1.0`에도 Rail/Drawer가 없어 모든 화면 폭에서 최상위 내비게이션은 `FloatingGlassScaffold` + `FloatingBottomIsland`를 사용하고, 태블릿/확장 폭에서는 `AuraTheme.layout.gridColumns` 및 `AuraAdaptiveTwoPane`으로 콘텐츠 밀도를 조정합니다.
