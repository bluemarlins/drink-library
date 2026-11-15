# Aura Design System 적용 결정 기록

- **대상:** `bluemarlins/aura-design-system` `1.0.0` (`com.example.ds`)
- **최종 확정일:** 2026-11-15
- **상태:** 적용 완료 (세부 명세는 [design-system.md](../../../specs/designer/design-system.md)로 승격)

## 확정 결정 사항

1. **로컬 AAR 참조 전환:** 초기 `:design-system` 모듈 벤더링을 제거하고 `app/libs/aura-design-system-1.0.0.aar` 로컬 참조로 전환 (`app/build.gradle.kts` + BOM `foundation`/`ui`/`ui-graphics`).
2. **Material 3 / Haze 완전 제거:** `androidx.compose.material3` 및 `dev.chrisbanes.haze` 의존성과 모든 import 제거.
3. **충돌 기준 통일 (Aura 우선):**
   - 기존 Serif `DisplayTasteCode` 예외를 폐기하고 Aura Sans-only (`AuraTheme.typography.displayMedium`, `headlineMedium`)로 통일.
   - 기존 `ChartWine*` / `ChartWhiskey*` 16진수 상수를 제거하고 `AuraBadgeVariant.Error` / `Warning`으로 통일 (`EVAL-002` 준수).
   - 하드코딩 여백·패딩을 `AuraTheme.layout`으로 통일.
4. **별점 0.5 단위 지원:** `AuraRatingBar(allowHalfRating = true)` 및 `DrinkLabels.rating` 적용.
5. **대체 불가(Gap) 컴포넌트 유지 및 별도 목록화:**
   - `Snackbar` (`DDSnackbarHost`, `DDSnackbarState`) — Aura 1.0.0에 없음 → 기존 유지
   - `DDProgressBar`, `DDProbeProgress` — Aura 1.0.0에 ProgressIndicator 없음 → 토큰 유지
   - `DDDivider` — Aura 1.0.0에 Divider 없음 → `border.subtle` 1dp 유지
   - `DDUriImage`, `DDShape`, 도메인 복합 카드/배지(`DDTasteTypeBadge`, `DDTasteSentenceCard`, `DDProfileProgressCard`, `DDProbeQuestion`, `DDDrinkBadge`, `DDRepurchaseBadge`)

## 후속 작업

- `Search`(F5) 화면 구현 시 `DDScreenScaffold`의 `onSearchClick`과 `AuraSearchBar`를 연결한다.
