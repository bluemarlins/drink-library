# Aura Design System 1.0.0 미지원 컴포넌트 목록 (Unsupported / Gap Components)

- **기준 AAR:** `app/libs/aura-design-system-1.0.0.aar` (`bluemarlins/aura-design-system` `v1.0.0`, package `com.example.ds`)
- **작성일:** 2026-11-15
- **관련 문서:** [design-system.md](../../../specs/designer/design-system.md), [aura-adoption.md](./aura-adoption.md)

---

## 1. Aura 1.0.0에 존재하지 않는 컴포넌트 (직접 대체 불가)

아래 항목은 `aura-design-system-1.0.0.aar`의 `classes.jar`를 전수 확인한 결과 대응 UI 컴포넌트가 제공되지 않아 앱 로컬 구현을 유지하되, 내부 색상·타이포·모션은 `AuraTheme.*` 토큰만 사용하도록 구성한 항목입니다.

| # | 항목 / 용도 | 앱 로컬 구현 | 미지원 사유 | 적용 방식 및 위치 |
| --- | --- | --- | --- | --- |
| 1 | **Snackbar / Toast** (오류·상태 알림 배너) | `DDSnackbarHost`, `DDSnackbarState` | Aura `1.0.0`에 `Snackbar` / `Toast` 컴포넌트 없음 | [`DDSnackbar.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDSnackbar.kt) — 기존 로컬 스낵바 유지. `AuraTheme.colors.surface.inverse`, `content.inverse`, `typography.bodyMedium`, `motion.stateChangeDurationMillis` 사용. 최상위 화면에서는 `FloatingBottomIsland`와 겹치지 않도록 `bottomPadding` 확보. |
| 2 | **Linear Progress Bar** (취향 판정 준비도 진행 막대) | `DDProgressBar` | Aura `1.0.0`에 `LinearProgressIndicator` / `ProgressBar` 없음 | [`DDCards.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDCards.kt) — `AuraTheme.colors.surface.sunken` + `brand.primary` 조합의 로컬 Box 구현 유지. |
| 3 | **Step Segmented Progress** (다단계 입력 진행 막대) | `DDProbeProgress` | Aura `1.0.0`에 Step Progress Bar 컴포넌트 없음 | [`ProbeComponents.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/ProbeComponents.kt) — 단계별 `AuraTheme.colors.brand.primary` / `surface.sunken` 표시. |
| 4 | **Horizontal Divider** (섹션 구분선) | `DDDivider` | Aura `1.0.0`에 독립 `Divider` 컴포넌트 없음 | [`DDCards.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDCards.kt) — `1.dp` 높이 + `AuraTheme.colors.border.subtle` 사용. |
| 5 | **Local URI Image Loader** (Content URI 비동기 이미지 디코더) | `DDUriImage` | Android `ContentResolver`/`ImageDecoder` 기반 이미지 로더는 디자인 시스템 범위 외 | [`DDUriImage.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDUriImage.kt) — 로컬 비동기 디코더 유지. |
| 6 | **Shape 토큰 (`AuraTheme.shapes` 부재)** | `DDShape` (`4/8/12/16.dp`) | Aura `1.0.0`은 컴포넌트 내부에 corner radius를 캡슐화하고 공용 `Shape` 토큰을 노출하지 않음 | [`DDShape.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/theme/DDShape.kt) — Gap 컴포넌트(`DDSnackbarHost`, `DDProgressBar`, `DDProbeProgress`) 및 이미지 클립 전용 유지. |

---

## 2. 도메인 복합 컴포넌트 (Aura 컴포넌트 조합으로 유지)

단일 UI 프리미티브가 아닌 DrinkDiary 도메인 전용 조합 UI로, 내부는 모두 Aura 컴포넌트와 토큰을 사용합니다.

| 항목 | 내부 사용 Aura 요소 | 위치 |
| --- | --- | --- |
| `DDDrinkBadge` | `AuraBadge` (`Error` = 와인, `Warning` = 위스키) | [`DDBadges.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDBadges.kt) |
| `DDRepurchaseBadge` | `AuraBadge` (`Brand`) | [`DDBadges.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDBadges.kt) |
| `DDTasteTypeBadge` | `AuraText(displayMedium)`, `AuraBadge(Neutral)` | [`DDCards.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDCards.kt) |
| `DDTasteSentenceCard` | `AuraCard(Outlined)`, `AuraText(headlineMedium, bodyMedium)` | [`DDCards.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDCards.kt) |
| `DDProfileProgressCard` | `AuraCard(Outlined)`, `AuraText`, `DDProgressBar`(Gap) | [`DDCards.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDCards.kt) |
| `DDProbeQuestion` | `AuraCard(Outlined)`, `AuraText`, `AuraTheme.layout` | [`ProbeComponents.kt`](../../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/ProbeComponents.kt) |

---

## 3. 내비게이션 구조 참고 사항

- **NavigationRail / NavigationDrawer 미제공:** Aura `1.0.0`에 Rail/Drawer가 없어 모든 화면 폭에서 최상위 내비게이션은 `FloatingGlassScaffold` + `FloatingBottomIsland`를 사용하고, 태블릿/확장 폭에서는 `AuraTheme.layout.gridColumns` 및 `AuraAdaptiveTwoPane`으로 콘텐츠 밀도를 조정합니다.
