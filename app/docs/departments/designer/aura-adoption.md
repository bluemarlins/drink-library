# Aura Design System 전면 도입 (Designer 초안)

> 상태: **초안 — 사용자 지시로 코드 이행과 병행 진행.** `specs/designer/`로의 승격(= 기존 명세 교체)은 이행 완료 후 별도 기록한다.

## 결정 (2026-11-15, 사용자 지시)

1. **Aura를 전적으로 도입**하고 충돌하는 기존 디자인 원칙은 수정하거나 폐기한다.
2. 빌드 버전 상향 (Kotlin 2.2.10, KSP 2.2.10-2.0.2, Compose BOM 2025.02.00).
3. 아이콘은 자체 제작 Vector Drawable (24dp, 2dp 스트로크, 라운드 캡/조인).
4. `haze` 의존성 제거 — 글래스 효과는 Aura `GlassSurface` 사용.
5. Aura `:design-system`을 저장소에 vendoring (`design-system/`, 패키지 `com.example.ds` 유지). 원본 문서는 `aura-upstream/`.

## 폐기 / 수정되는 기존 원칙

| 기존 (`specs/designer/design-system.md`) | 처리 |
|---|---|
| 에디토리얼·웜 무드, Paper/Wine/Malt 팔레트, 다크 `#15110E` | **폐기** → Aura Cool Gray + Blue (`AuraPalette`) |
| 앱 전체 액센트 2개 / 60-30-10 | **수정** → Aura `brand.primary` 단일 액센트 + `status.*` |
| Serif+Sans 2종 | **수정** → Aura Sans 기본. Serif는 81유형 코드(`DisplayTasteCode`)에만 예외 유지 |
| 코드 내 Hex 하드코딩 금지 | **유지** (Aura EVAL-002와 일치) |
| 최소 48dp 터치, 4/8dp 그리드 | **유지** (Aura EVAL-005와 일치) |
| Material3 기반 | **폐기** → zero-Material3 |
| Rail(Medium) / Drawer(Expanded) | **수정** → 전 구간 `AuraBottomIsland` + `AuraAdaptiveTwoPane` (Rail/Drawer 미도입) |
| Material You dynamic color | **폐기** |
| 와인/위스키 구분색 | **유지(로컬 토큰)** — Aura에 대응물 없음 |

## Aura에 대응 컴포넌트 없음 (GAP)과 처리

| 항목 | 처리 |
|---|---|
| Snackbar | `design-system`에 `AuraSnackbar` 추가 |
| ProgressBar / Stepper | 앱 로컬 `DDProgressBar`, `DDProbeProgress` (Aura 토큰 사용) |
| Divider, Loading, Empty | 앱 로컬 `DDDivider`, `DDLoadingContent`, `DDEmptyContent` |
| 이미지(`DDUriImage`), 공유 카드, `TraitStatusRow` | 앱 로컬 유지, 토큰만 교체 |
| Shape 토큰 | Aura 컴포넌트 내부 값을 따르고 앱 로컬은 상수 사용 |
| Rail / Drawer / BottomSheet | 미도입 (위 표) |
