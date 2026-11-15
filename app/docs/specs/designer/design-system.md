# Design System — Aura Design System 적용 및 앱 로컬 Gap 규격

**최종 수정일:** 2026-11-15  
**목적:** `app` 모듈 UI 전체에 **Aura Design System (`v1.0.0`, `com.example.ds`)**을 단일 디자인 시스템 소스로 적용하고, Aura와 충돌하던 기존 DrinkDiary 디자인 규격을 정리하며, Aura 1.0.0으로 대체 불가능한 로컬 Gap 컴포넌트를 명시한다.

---

## 1. 적용 방식 및 소스

- **참조 방식:** 로컬 AAR 참조 (`app/libs/aura-design-system-1.0.0.aar`, 출처: `bluemarlins/aura-design-system` v1.0.0 release).
  - Compose BOM 기반 `ui`, `ui-graphics`, `foundation`, `core-ktx`만 사용한다.
  - **`androidx.compose.material3` 및 `dev.chrisbanes.haze`는 완전히 제거한다** (Gradle 의존성 및 Kotlin import 0건).
  - 앱 루트는 `DrinkDiaryTheme` → `AuraTheme(colorMode = ...)`로 연결한다.
- **기존 규격과의 충돌 해결 (Aura 기준 통일):**
  - **색상 (`02-color.md`):** 기존 Warm Paper(`#FFF8F2`), Wine/Malt 별도 팔레트 및 `ChartWine*`/`ChartWhiskey*` 16진수 상수는 `EVAL-002`(하드코딩 헥스 금지)와 충돌하므로 제거했다. 모든 표면·텍스트·브랜드·상태 색은 `AuraTheme.colors`를 사용하며, 와인/위스키 구분 배지는 `AuraBadgeVariant.Error` / `AuraBadgeVariant.Warning`으로 매핑한다.
  - **타이포그래피 (`04-typography.md`):** Aura는 Sans-only 정책을 따른다. 기존 `DisplayTasteCode`(Serif)와 `HeadlineSentence` 예외 토큰을 제거하고 `AuraTheme.typography.displayMedium` 및 `AuraTheme.typography.headlineMedium`으로 통일했다.
  - **레이아웃 (`05-responsive-layout.md`):** 화면별 하드코딩 여백(`20.dp`/`24.dp`/`96.dp`) 대신 `AuraTheme.layout`(`screenMarginHorizontal`, `screenMarginVertical`, `sectionSpacing`, `gridGutter`, `gridColumns`, `supportsTwoPane`)을 사용한다.
  - **내비게이션:** Rail/Drawer 없이 모든 폭에서 최상위는 `FloatingGlassScaffold` + `AuraTopAppBar(FloatingCapsule, TranslucentGlass)` + `FloatingBottomIsland(GlassStyle.LIQUID, splitAction = "+" / 기록하기)`을 사용하고, 상세/편집은 `AuraScaffold` + `AuraTopAppBar(SurfaceDocked, SolidElevated)`를 사용한다.

---

## 2. Aura 컴포넌트 매핑

| 화면 / 역할 | 적용 Aura 컴포넌트 | 비고 |
| --- | --- | --- |
| 테마 루트 | `AuraTheme` | Light / Dark 자동 전환 |
| 최상위 스캐폴드 / 바 | `FloatingGlassScaffold`, `AuraTopAppBar(FloatingCapsule, TranslucentGlass)`, `FloatingBottomIsland` | 추가 기록 버튼은 `FloatingBottomIsland` Split Action(`isActionSplit = true`) 사용 |
| 상세/편집 스캐폴드 | `AuraScaffold`, `AuraTopAppBar(SurfaceDocked, SolidElevated)` | 뒤로 가기는 `AuraIconButton(Ghost)` |
| 버튼 / 아이콘 버튼 | `AuraButton`, `AuraIconButton`, `AuraIcon` | 기존 1:1 래퍼(`DDPrimaryButton` 등) 제거, 직접 호출 |
| 카드 | `AuraCard` (`Outlined` / `None`) | 취향 요약, 컬렉션 행, 주종/프로브 선택 |
| 배지 (상태·주종·취향 결과) | `AuraBadge` | `Brand`, `Neutral`, `Error`(와인), `Warning`(위스키) |
| 세그먼트 전환 | `AuraSegmentedButton` | 대시보드 범위, 컬렉션 필터, 재구매 의사 선택 |
| 칩 선택 | `AuraChipButton` | 취향 축 편집, 라벨 태그 다중 선택, 음용 방식 |
| 텍스트 입력 | `AuraTextField` | 술 이름·장소는 `Outlined`, 메모는 `Paperless` 다중 행 |
| 숫자 입력 | `AuraNumberField` | 빈티지(4자리 정수), 가격(`suffixText = "원"`, `formatThousands = true`) |
| 별점 입력 / 읽기 전용 표시 | `AuraRatingBar` | `allowHalfRating = true` (`0.5` 단위 허용), 상세는 `onRatingChange = null` 읽기 전용 |
| 스위치 | `AuraSwitch` | 설정 화면 태그 자동 질문 토글 |
| 삭제 확인 다이얼로그 | `AuraDialog` (`AuraDialogVariant.Destructive`) | 기록 삭제 확인 |
| 적응형 그리드 / 2단 레이아웃 | `LazyVerticalGrid(GridCells.Fixed(layout.gridColumns))`, `AuraAdaptiveTwoPane` | 대시보드·상세 `supportsTwoPane` 대응 |

---

## 3. 별점 0.5 단위 정책

- `AuraRatingBar(allowHalfRating = true)`로 `0.5` ~ `5.0` 범위의 반 개 단위 입력을 허용한다.
- 저장 가능 조건(`RecordForm.isSavable`)은 `name.isNotBlank() && rating > 0.0`이며, 표시 문자열은 `DrinkLabels.rating(Double)`을 통해 정수는 소수점 없이(`"4"`), 반 단위는 소수 첫째 자리까지(`"4.5"`, `"0.5"`) 표시해 소수 정보가 유실되지 않게 한다.

---

## 4. Aura 1.0.0으로 대체 불가능한 컴포넌트 목록 (Non-Replaceable / Gap List)

Aura `1.0.0` AAR에 대응 컴포넌트가 없거나 도메인 조합이 필요해 로컬 구현(Aura 토큰 전용)으로 유지한 항목:

1. **Snackbar (`DDSnackbarHost`, `DDSnackbarState`)** — *사용자 지정 유지 항목*
   - 사유: `aura-design-system-1.0.0.aar`에 `Snackbar` / `Toast` 컴포넌트가 없음.
   - 구현: `ui/component/DDSnackbar.kt`에 기존 로컬 스낵바 유지. `AuraTheme.colors.surface.inverse`, `content.inverse`, `AuraTheme.typography.bodyMedium`, `AuraTheme.motion.stateChangeDurationMillis`만 사용하며 최상위 화면에서는 `FloatingBottomIsland` 높이를 피해 배치한다.
2. **선형 진행률 바 (`DDProgressBar`) & 단계 진행 막대 (`DDProbeProgress`)**
   - 사유: Aura 1.0.0에 `LinearProgressIndicator` / Step Progress 컴포넌트가 없음.
   - 구현: `AuraTheme.colors.surface.sunken`, `brand.primary`, `DDShape`만 사용하는 토큰 기반 Box 조합 유지.
3. **수평 구분선 (`DDDivider`)**
   - 사유: Aura 1.0.0에 독립 `Divider` 컴포넌트가 없음.
   - 구현: `1.dp` 높이 + `AuraTheme.colors.border.subtle` 사용.
4. **로컬 URI 이미지 로더 (`DDUriImage`)**
   - 사유: Aura는 디자인 시스템 컴포넌트만 제공하며 Android `ContentResolver`/`ImageDecoder` 기반 비동기 로컬 이미지 디코더를 포함하지 않음.
   - 구현: `ui/component/UriImage.kt` 유지.
5. **Shape 토큰 (`DDShape`)**
   - 사유: Aura는 컴포넌트 내부에 모서리 반경을 캡슐화하고 `AuraTheme.shapes`를 노출하지 않음.
   - 구현: Gap 컴포넌트와 이미지 클립에 한해 `4/8/12/16.dp` (`DDShape`) 사용.
6. **도메인 복합 컴포넌트 (`DDDrinkBadge`, `DDRepurchaseBadge`, `DDTasteTypeBadge`, `DDTasteSentenceCard`, `DDProfileProgressCard`, `DDProbeQuestion`)**
   - 사유: 앱 도메인 특화 복합 UI. 내부는 모두 `AuraCard`, `AuraBadge`, `AuraText`, `AuraTheme.*` 및 `DDProgressBar`로 구성한다.
