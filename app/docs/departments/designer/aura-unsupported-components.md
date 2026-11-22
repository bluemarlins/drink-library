# Aura Design System 미지원 컴포넌트 목록 (Unsupported / Gap Components)

- **기준 AAR:** `app/libs/aura-design-system-2.1.0.aar` (`bluemarlins/aura-design-system` `2.1.0`, package `io.github.bluemarlins.aura`)
- **최종 갱신일:** 2026-11-22
- **관련 문서:** [design-system.md](../../specs/designer/design-system.md), [aura-adoption.md](./aura-adoption.md), [aura-usage-scorecard.md](./aura-usage-scorecard.md)

앱이 Aura 밖에서 직접 그리는 UI는 모두 이 문서에 사유가 있어야 한다. [Aura 사용 점수표](./aura-usage-scorecard.md) 1축(채택률)이 이 목록을 기준으로 판정한다.

---

## 1. Aura에 추가되어 해소된 항목 (Resolved)

| # | 기존 Gap 항목 | 대체한 Aura API | 해소 버전 | 비고 |
| --- | --- | --- | --- | --- |
| 1 | `DDSnackbarHost`, `DDSnackbarState` | `AuraSnackbarHost`, `rememberAuraSnackbarHostState`, `FloatingGlassScaffold(snackbarHost = ...)` | 1.1.0 | `DDSnackbar.kt` 삭제, 하단 Island 회피 자동 처리 |
| 2 | `DDProgressBar` | `AuraLinearProgressBar` | 1.1.0 | 요약 탭 취향 진행 카드 |
| 3 | `DDProbeProgress` | `AuraStepProgressBar` | 1.1.0 | `ProbeSequenceScreen.kt` |
| 4 | `DDDivider` | `AuraDivider` | 1.1.0 | 전 화면 |
| 5 | `DDShape` | `AuraTheme.shapes` | 1.1.0 | `DDShape.kt` 삭제 |
| 6 | 일지 필터 시트 | `AuraDropdownMenu` + `AuraTopAppBar(expandedSubtitle)` | 1.2.0 | `CollectionFilterMenu.kt` |
| 7 | 작업 화면 하단 저장 버튼 | `AuraFloatingActionBar` | 1.3.0 | 기록하기·기록 고치기·기록 상세 |
| 8 | 요약 로딩 빈 상태 | `AuraSkeleton`, `AuraSkeletonText` | 1.3.0 | `ProfileScreen.kt` |

---

## 2. 로컬에서 직접 그리는 항목 (Aura 미지원)

| # | 항목 / 용도 | 앱 로컬 구현 | 유지 사유 | 위치 |
| --- | --- | --- | --- | --- |
| 1 | **Content URI 이미지** | `DDUriImage` | `ContentResolver`/`ImageDecoder` 기반 이미지 디코딩은 디자인 시스템 범위 밖이다. 로딩 전 자리는 `surface.sunken`, 모양은 `AuraTheme.shapes`를 쓴다. | [`DDUriImage.kt`](../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDUriImage.kt) |
| 2 | **이번 달 주간 막대 그래프** | `SummaryCards`의 주별 막대 | Aura에 차트 컴포넌트가 없다. 막대는 `brand.primary`(가장 많은 주) / `brand.subtle`, 모양은 `shapes.small`만 쓴다. | [`SummaryCards.kt:104`](../../../src/main/java/com/bluemarlin/drinkdiary/ui/profile/SummaryCards.kt) |
| 3 | **맛 기준 극 막대 (`PoleBar`)** | `SummaryCards.PoleBar` | 가운데에서 한쪽 극으로 뻗는 양방향 막대다. `AuraLinearProgressBar`는 시작점에서만 채워 이 모양을 그릴 수 없다. 트랙 `surface.sunken`, 채움 `brand.primary`, 높이 `6dp`, `shapes.small`로 진행 막대와 같은 토큰을 쓴다. | [`SummaryCards.kt:307`](../../../src/main/java/com/bluemarlin/drinkdiary/ui/profile/SummaryCards.kt) |

---

## 3. 도메인 복합·레이아웃 컴포넌트 (Aura 조합으로 유지)

직접 그리는 부분이 없고 내부는 모두 Aura 컴포넌트와 토큰이다.

| 항목 | 내부 사용 Aura 요소 | 로컬로 두는 이유 | 위치 |
| --- | --- | --- | --- |
| `DDDrinkBadge` | `AuraBadge` (`Error` = 와인, `Warning` = 위스키) | 술 종류 → 배지 색 매핑은 도메인 규칙 | [`DDBadges.kt`](../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDBadges.kt) |
| `DDRepurchaseBadge` | `AuraBadge` (`Brand`) | "또 살래요" 문구 | [`DDBadges.kt`](../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDBadges.kt) |
| `DDBentoGrid` | 간격은 호출부가 `AuraTheme.layout` 값으로 넘긴다 | 카드마다 가로 칸 수(`span`)가 다른 벤토 배치. `AuraAdaptiveGrid`는 같은 크기 칸만 지원한다. | [`DDBentoGrid.kt`](../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDBentoGrid.kt) |
| `DDCenteredContent` | `AuraTheme.layout` (`maxContentWidth`, 화면 여백) | 화면 여백·섹션 간격을 적용하고 넓은 화면에서 본문을 가운데 최대 폭으로 묶어 스크롤 | [`DDResponsive.kt`](../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDResponsive.kt) |
| `DDTwoPaneContent` | `AuraAdaptiveTwoPane` | 펼친 화면 두 칸 배치에 앱 여백을 더한 얇은 래퍼 | [`DDResponsive.kt`](../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDResponsive.kt) |
| `DDProbeQuestion` | `AuraCard(Outlined)`, `AuraText` | 맛 질문 카드 문구 배치 | [`ProbeComponents.kt`](../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/ProbeComponents.kt) |

---

## 4. 내비게이션 구조 참고 사항

- **NavigationRail / NavigationDrawer 미제공:** Aura `2.1.0`에도 Rail/Drawer가 없어 모든 화면 폭에서 최상위 내비게이션은 `FloatingGlassScaffold` + `FloatingBottomIsland`를 쓰고, 펼친 화면에서는 `AuraTheme.layout.gridColumns`와 `AuraAdaptiveTwoPane`으로 콘텐츠 밀도를 조정한다.
