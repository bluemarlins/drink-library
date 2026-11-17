# Design System — Aura Design System 적용 및 앱 로컬 Gap 규격

**최종 수정일:** 2026-11-16  
**목적:** `app` 모듈 UI 전체에 **Aura Design System (`v1.1.0`, `com.example.ds`)**을 단일 디자인 시스템 소스로 적용하고, Aura와 충돌하던 기존 DrinkDiary 디자인 규격을 정리하며, 앱 로컬 유지 항목을 명시한다.

---

## 1. 적용 방식 및 소스

- **참조 방식:** 로컬 AAR 참조 (`app/libs/aura-design-system-1.1.0.aar`, 출처: `bluemarlins/aura-design-system` v1.1.0).
  - Compose BOM 기반 `ui`, `ui-graphics`, `foundation`, `core-ktx`만 사용한다.
  - **`androidx.compose.material3` 및 `dev.chrisbanes.haze`는 완전히 제거한다** (Gradle 의존성 및 Kotlin import 0건).
  - 앱 루트는 `DrinkDiaryTheme` → `AuraTheme(colorMode = ..., material = AuraMaterial.LiquidGlass, glass = AuraGlassDefaults.forColorMode(...))`로 연결한다. (`AuraTheme.material`은 하위 모든 Glass 서피스의 최대 상한(Ceiling)으로 동작하며, `reduceTransparency` 활성 시 자동으로 `Solid`(`solidFallbackAlpha = 0.98f`)로 폴백한다.)
- **기존 규격과의 충돌 해결 (Aura 기준 통일):**
  - **색상 (`02-color.md`):** 기존 Warm Paper(`#FFF8F2`), Wine/Malt 별도 팔레트 및 `ChartWine*`/`ChartWhiskey*` 16진수 상수는 `EVAL-002`(하드코딩 헥스 금지)와 충돌하므로 제거했다. 모든 표면·텍스트·브랜드·상태 색은 `AuraTheme.colors`를 사용하며, 와인/위스키 구분 배지는 `AuraBadgeVariant.Error` / `AuraBadgeVariant.Warning`으로 매핑한다.
  - **타이포그래피 (`04-typography.md`):** Aura는 Sans-only 정책을 따른다. 기존 `DisplayTasteCode`(Serif)와 `HeadlineSentence` 예외 토큰을 제거하고 `AuraTheme.typography.displayMedium` 및 `AuraTheme.typography.headlineMedium`으로 통일했다.
  - **레이아웃 (`05-responsive-layout.md`):** 화면별 하드코딩 여백(`20.dp`/`24.dp`/`96.dp`) 대신 `AuraTheme.layout`(`screenMarginHorizontal`, `screenMarginVertical`, `sectionSpacing`, `gridGutter`, `gridColumns`, `maxContentWidth`, `supportsTwoPane`)을 사용한다. 단일 열 폼/안내 화면과 2단 레이아웃 모두 [`DDResponsive.kt`](../../../src/main/java/com/bluemarlin/drinkdiary/ui/component/DDResponsive.kt)(`DDCenteredContent`, `DDTwoPaneContent`)를 통해 `maxContentWidth`로 최대 폭을 제한·중앙 정렬한다.
  - **내비게이션:** Rail/Drawer 없이 모든 폭에서 최상위(`TopLevel`)는 `FloatingGlassScaffold` + `AuraTopAppBar(AdaptiveFloating, TranslucentGlass)` + `FloatingBottomIsland(GlassStyle.FROSTED, splitAction = "+" / 기록하기)`을 사용하고, 상세(`Detail`)는 `FloatingGlassScaffold(hasBottomIsland = false)` + `AuraTopAppBar(AdaptiveFloating, TranslucentGlass)`를 사용하며, 편집(`Editor`)은 `AuraScaffold` + `AuraTopAppBar(SurfaceDocked, SolidElevated)`를 사용한다.
    - 하단 바는 `FROSTED`다(2026-11-17 사용자 결정, `LIQUID`에서 변경). `LIQUID`는 뒤를 흐리지 않고 굴절만 해서, 대시보드 슬라이더의 점과 눈금이 바 안에 그대로 비쳐 선택 표시처럼 읽혔다(`../../departments/qa/device-review-2026-11-17.md` P1-5). 테마 상한 `LiquidGlass`는 그대로다.

---

## 2. Aura 컴포넌트 매핑

| 화면 / 역할 | 적용 Aura 컴포넌트 | 비고 |
| --- | --- | --- |
| 테마 루트 | `AuraTheme`, `AuraGlass` | Light / Dark 자동 전환, `LiquidGlass` 상한(Ceiling) 및 `rememberAuraReduceTransparency()` 실시간 감시 |
| 최상위 스캐폴드 / 바 | `FloatingGlassScaffold`, `AuraTopAppBar(AdaptiveFloating, TranslucentGlass)`, `FloatingBottomIsland` | 스크롤 시 타이틀 페이드아웃 후 좌우 버튼 개별 알약 변형, 추가 기록 버튼은 `FloatingBottomIsland` Split Action(`isActionSplit = true`) 사용 |
| 상세 스캐폴드 / 바 | `FloatingGlassScaffold(hasBottomIsland = false)`, `AuraTopAppBar(AdaptiveFloating, TranslucentGlass)` | 뒤로 가기 `AuraIconButton(Ghost)`가 스크롤 시 개별 Glass 알약으로 변형 |
| 편집 스캐폴드 | `AuraScaffold`, `AuraTopAppBar(SurfaceDocked, SolidElevated)` | 고밀도 입력 폼 전용, 뒤로 가기는 `AuraIconButton(Ghost)` |
| 버튼 / 아이콘 버튼 | `AuraButton`, `AuraIconButton`, `AuraIcon` | 기존 1:1 래퍼(`DDPrimaryButton` 등) 제거, 직접 호출 |
| 카드 | `AuraCard` (`Outlined` / `None`) | 취향 요약, 컬렉션 행, 주종/프로브 선택 |
| 배지 (상태·주종·취향 결과) | `AuraBadge` | `Brand`, `Neutral`, `Error`(와인), `Warning`(위스키) |
| 세그먼트 전환 | `AuraSegmentedButton` | 대시보드 범위, 컬렉션 필터, 재구매 의사 선택 |
| 칩 선택 | `AuraChipButton` | 취향 축 편집, 라벨 태그 다중 선택, 음용 방식 |
| 텍스트 입력 / 검색바 | `AuraTextField`, `AuraSearchBar(Solid)` | 술 이름·장소는 `Outlined`, 메모는 `Paperless`, 컬렉션/매장 찾기는 `AuraSearchBar` |
| 숫자 입력 | `AuraNumberField` | 빈티지(4자리 정수), 가격(`suffixText = "원"`, `formatThousands = true`) |
| 별점 입력 / 읽기 전용 표시 | `AuraRatingBar` | `allowHalfRating = true` (`0.5` 단위 허용), 상세는 `onRatingChange = null` 읽기 전용 |
| 다이얼로그 (삭제·이탈·공유 미리보기) | `AuraDialog` (`Destructive` / `Standard` / `Glass`) | 기록 삭제 확인(`Destructive`), 기록 이탈 확인(`Standard`), 9:16 취향 공유 카드 미리보기(`Glass`, `level4`) |
| 구분선 / 진행 막대 / 스낵바 / 모서리 토큰 | `AuraDivider`, `AuraLinearProgressBar`, `AuraStepProgressBar`, `AuraSnackbarHost`, `AuraTheme.shapes` | `1.1.0` 편입으로 기존 Gap(`DDDivider`, `DDProgressBar`, `DDProbeProgress`, `DDSnackbarHost`, `DDShape`) 전면 대체, 플로팅 화면 스낵바는 `AuraMaterial.Glass`(`level3`) 적용 |
| 적응형 그리드 / 2단 레이아웃 | `AuraAdaptiveGrid`, `AuraAdaptiveTwoPane`, `LazyVerticalGrid(GridCells.Fixed(layout.gridColumns))` | 주종 선택 그리드(`DrinkPicker`), 대시보드·상세·주종 선택 `supportsTwoPane` 및 `maxContentWidth` 대응 |

---

## 3. 별점 0.5 단위 정책

- `AuraRatingBar(allowHalfRating = true)`로 `0.5` ~ `5.0` 범위의 반 개 단위 입력을 허용한다.
- 저장 가능 조건(`RecordForm.isSavable`)은 `name.isNotBlank() && rating > 0.0`이며, 표시 문자열은 `DrinkLabels.rating(Double)`을 통해 정수는 소수점 없이(`"4"`), 반 단위는 소수 첫째 자리까지(`"4.5"`, `"0.5"`) 표시해 소수 정보가 유실되지 않게 한다.

---

## 4. 앱 로컬 유지 컴포넌트 목록 (Non-Design-System Scope)

Aura `1.1.0` 편입으로 `DDSnackbarHost`, `DDProgressBar`, `DDProbeProgress`, `DDDivider`, `DDShape`는 모두 제거되었으며, 아래 항목만 앱 로컬 구현(Aura 토큰 전용)으로 유지합니다:

1. **로컬 URI 이미지 로더 (`DDUriImage`)**
   - 사유: Aura는 디자인 시스템 컴포넌트만 제공하며 Android `ContentResolver`/`ImageDecoder` 기반 비동기 로컬 이미지 디코더를 포함하지 않음.
   - 구현: `ui/component/DDUriImage.kt` 유지 (`AuraTheme.shapes`로 클립).
2. **도메인 복합 컴포넌트 (`DDDrinkBadge`, `DDRepurchaseBadge`, `DDTasteTypeBadge`, `DDTasteSentenceCard`, `DDProfileProgressCard`, `DDProbeQuestion`, `DDDashboardSummaryCard`, `DDTraitSpectrumRow`, `DDBuyRecommendationSection`)**
   - 사유: 앱 도메인 특화 복합 UI. 내부는 모두 `AuraCard`, `AuraBadge`, `AuraText`, `AuraLinearProgressBar`, `AuraTheme.*`로 구성한다.
