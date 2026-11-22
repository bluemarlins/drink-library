# Aura 사용 점수표 (Aura Usage Scorecard)

앱이 Aura Design System을 "잘 쓰고 있는지"를 네 축으로 나눠 화면별로 잰다. 합산 점수는 두지 않는다. 어느 축이 약한지가 바로 보여야 하기 때문이다.

- **대상:** `aura-design-system` `2.1.0` (`app/libs/aura-design-system-2.1.0.aar`). 기준선은 `2.0.0`에서 쟀다.
- **범위:** 사용자가 실제로 보는 주요 흐름 — 요약, 일지, 기록하기, 기록 상세·고치기, 설정, 공통 크롬. Probe 화면과 공유 카드는 제외.
- **판정:** 1–3축은 도구가 판정하고, 4축은 Claude가 Spec 규칙 ID별로 판정해 근거(코드 위치)를 남긴다. 애매한 항목은 "판단 보류"로 두고 사람이 정한다.

## 1. 평가 축

| 축 | 질문 | 측정 | 통과 기준 | 회귀 방지 |
|---|---|---|---|---|
| **1. 채택률** | Aura 밖에서 직접 그린 UI가 있나? | 화면 파일의 UI 호출을 Aura / 직접 그림(`BasicText`, `Image`, `Canvas`, `.background`, `.clickable` 등)으로 분류 | 직접 그린 곳은 모두 [`aura-unsupported-components.md`](aura-unsupported-components.md)에 사유가 있을 것 | 이 문서 (분기마다 재측정) |
| **2. 규칙 위반** | 토큰 대신 값을 직접 쓴 곳이 있나? | `verify-aura-app.ps1`(`EVAL-002`, `MOT-RULE-002`, `VIS-RULE-001/006`) + [`scripts/verify-aura-specs.ps1`](../../../../scripts/verify-aura-specs.ps1)(`CHK-*` 5개) | 위반 0건 | 스크립트 `-FailOnViolation` |
| **3. 렌더링 계약** | 실제 화면이 Spec대로 그려지나? | [`AuraRenderContractTest`](../../../src/test/java/com/bluemarlin/drinkdiary/ui/AuraRenderContractTest.kt) — 8개 화면 × 5개 프로필(ExtraSmall, Compact, Square, UltraWide, Expanded)에서 터치 영역 `>= 48dp`, 글자 세로 잘림, 터치 영역 겹침 검사 | 위반 0건 | 단위 테스트 (빌드 실패) |
| **4. 용도 적합성** | 상황에 맞는 컴포넌트·변형을 골랐나? | 쓰인 컴포넌트 Spec의 `When NOT to Use`, `Context Rules`, `Do / Don't`를 화면별로 판정 | 통과 / 해당 규칙 수 | 이 문서 |

## 2. 점수표 (2026-11-22, Aura 2.0.0)

| 화면 | 1. 채택률 | 2. 규칙 위반 | 3. 렌더링 계약 | 4. 용도 적합성 |
|---|---|---|---|---|
| 요약 | Aura 35 · 직접 그림 2곳 **(사유 미기재)** | 0 | 통과 | 12/12 (보류 1) |
| 일지 | Aura 13 · 직접 그림 0 | 0 | 통과 (Aura 상류 문제 1) | 13/13 |
| 기록하기 | Aura 37 · 직접 그림 0 | 0 | 통과 | **17/19** (보류 1) |
| 기록 상세·고치기 | Aura 23 · 직접 그림 0 | 0 | 통과 | 10/10 (고치기 폼은 기록하기 결과를 공유) |
| 설정 | Aura 16 · 직접 그림 0 | 0 | 통과 (경고 1) | **7/8** |
| 공통 크롬 | Aura 13 · 직접 그림 1곳 (사유 있음) | 0 | 통과 | 12/12 |

### 개선 후 (2026-11-22, A2–A5 반영)

| 화면 | 1. 채택률 | 2. 규칙 위반 | 3. 렌더링 계약 | 4. 용도 적합성 |
|---|---|---|---|---|
| 요약 | Aura 35 · 직접 그림 2곳 (사유 있음) | 0 | 통과 | 12/12 (보류 1) |
| 일지 | Aura 13 · 직접 그림 0 | 0 | 통과 (Aura 상류 문제 1) | 13/13 |
| 기록하기 | Aura 37 · 직접 그림 0 | 0 | 통과 | 18/18 (보류 1, Aura Spec 공백 1) |
| 기록 상세·고치기 | Aura 23 · 직접 그림 0 | 0 | 통과 | 10/10 |
| 설정 | Aura 16 · 직접 그림 0 | 0 | 통과 (경고 0) | 8/8 |
| 공통 크롬 | Aura 13 · 직접 그림 1곳 (사유 있음) | 0 | 통과 | 12/12 |

### Aura 2.1.0 반영 후 (2026-11-22, U1·U2 해결)

| 화면 | 1. 채택률 | 2. 규칙 위반 | 3. 렌더링 계약 | 4. 용도 적합성 |
|---|---|---|---|---|
| 요약 | Aura 35 · 직접 그림 2곳 (사유 있음) | 0 | 통과 | 13/13 (H1 해결) |
| 일지 | Aura 13 · 직접 그림 0 | 0 | 통과 (상류 문제 0) | 13/13 |
| 기록하기 | Aura 37 · 직접 그림 0 | 0 | 통과 (경고 1, A6 수용) | 19/19 (H2 결정: 해당 없음) |
| 기록 상세·고치기 | Aura 23 · 직접 그림 0 | 0 | 통과 | 10/10 |
| 설정 | Aura 16 · 직접 그림 0 | 0 | 통과 | 8/8 |
| 공통 크롬 | Aura 13 · 직접 그림 1곳 (사유 있음) | 0 | 통과 | 12/12 |

- 렌더링 계약 테스트에 "기록 고치기 · 위스키"(답하지 않은 지표가 섞인 상태)를 더해 9개 화면을 본다.
- 기록하기의 `SEG-RULE-001`은 맛 기준 답·마시는 방법을 `AuraSegmentedButton(selectedIndex = -1)`로 바꿔 통과로 돌렸다.

## 3. 발견 사항

### 앱에서 고칠 것

| # | 축 | 화면 | 규칙 | 내용 | 위치 | 처리 |
|---|---|---|---|---|---|---|
| A1 | 4 | 기록하기·고치기 | `SEG-RULE-001` | 맛 기준 답과 마시는 방법이 배타적 선택인데 `AuraChipButton(Tag)`로 그렸다. | `record/TasteEditor.kt:43`, `record/RecordDetailStep.kt:261` | **앱 위반 아님 → U2로 이동.** 두 값 모두 "아직 안 고름"(`null`)이 유효한데, `AuraSegmentedButton`은 항상 하나를 선택된 것으로 그린다. `Filter` 칩은 체크박스(다중 선택)로 읽힌다. 지금은 `selected` 상태를 가진 `Tag` 칩이 Aura 안에서 가장 맞는 선택이다. |
| A2 | 4 | 기록하기 | `CARD-RULE-003` | 종류 고르기 카드가 `AuraCardPadding.None`에 안쪽 여백 `24/18dp`를 따로 줬다. | `record/DrinkPicker.kt:174` | **고침.** `AuraCardPadding.Large`(`24dp`)로 바꾸고 텍스트 여백 제거 |
| A3 | 4·3 | 설정 | `SEG-RULE-002` | 재질 옵션 "리퀴드 글래스", "글래스모피즘"이 ExtraSmall(`320dp`)에서 말줄임으로 잘렸다. | `settings/SettingsScreen.kt:130` | **고침.** 라벨을 "리퀴드 / 글래스 / 솔리드"로 줄이고 설명은 아래 캡션이 맡는다. ExtraSmall 경고 0건 |
| A4 | 1 | 요약 | — | 월간 막대 그래프와 맛 기준 극 막대(`PoleBar`)의 사유가 미지원 목록에 없었다. | `profile/SummaryCards.kt:104`, `:307` | **고침.** [`aura-unsupported-components.md`](aura-unsupported-components.md) §2에 사유 추가 |
| A6 | 3·4 | 기록 고치기(위스키) | `SEG-RULE-002` | 피트 지표의 "스모키하지 않음"(8자)이 ExtraSmall(`320dp`)에서 말줄임으로 잘린다. 다른 프로필은 괜찮다. | `ui/DrinkLabels.kt:91` | **그대로 둠 (2026-11-22 결정).** 문구가 공유 카드와 같아야 하고, 잘리는 곳은 320dp 커버 화면뿐이다. 렌더링 테스트 경고로만 남긴다. |
| A5 | 1 | — | — | 미지원 목록이 Aura 1.1.0 기준이었고 없는 `DDCards.kt`, 깨진 상대 경로를 가리켰다. | 문서 | **고침.** 2.0.0 기준으로 다시 쓰고 1.2.0·1.3.0 해소 항목과 현재 로컬 컴포넌트를 반영 |

### Aura(상류)에서 고칠 것

| # | 축 | 내용 | 근거 | 앱 처리 |
|---|---|---|---|---|
| U1 | 3 | **2.1.0에서 해결.** `AuraSearchBar`는 한 줄 입력 영역(높이 `19dp`)만 포커스를 받는다. 52dp 바의 위·아래 여백을 눌러도 키보드가 뜨지 않는다. `AuraTextField`는 상자 전체가 입력 영역이라 문제가 없다. | `AuraRenderContractTest` 일지 결과 | 2.1.0에서 입력 영역이 52dp 바 전체가 되어 `knownUpstream`에서 지웠다. |
| U2 | 4 | **2.1.0에서 해결.** `SEG-RULE-001`은 배타적 2–5개 선택에 `AuraSegmentedButton`을 "항상" 쓰라고 하지만, 아직 고르지 않은 상태가 유효한 선택(설문 답, 선택 입력 항목)은 표현할 수 없다. 대안인 `AuraChipButton`도 `Filter`는 체크박스, `Tag`는 버튼으로 읽혀 "하나만 고르는 묶음"이라는 의미가 접근성에 드러나지 않는다. | 기록하기 맛 기준·마시는 방법 | Aura 2.1.0이 `selectedIndex = -1`(선택 없음)을 지원하고 Spec에 예외를 적었다. 앱은 `AuraSegmentedButton`으로 바꿨다. |

### 판단 보류 (사람이 정할 것)

| # | 규칙 | 질문 | 위치 |
|---|---|---|---|
| H1 | `SKL-RULE-002` | 요약 스켈레톤 높이(144/160/112dp)가 실제 카드 높이와 맞나? 데이터 길이에 따라 카드 높이가 변해 고정값으로는 판정할 수 없다. | `profile/ProfileScreen.kt:127` |
| H2 | `NUM-RULE-002` | 빈티지에 `showSteppers`(1950–2030)를 켤까? | `record/RecordDetailStep.kt:244` |

- **H1 실측 (2026-11-22, 갤럭시 폴더블 SM-F971N, 폭 555dp 화면, 2.25px/dp):** 앱을 새로 띄워 로딩 중과 로딩 후 화면을 캡처해 쟀다. 카드는 1dp 테두리를 포함한 바깥 높이다.

  | 칸 | 스켈레톤 | 실제 카드 | 차이 |
  |---|---|---|---|
  | 이번 달 | 144dp | 약 140dp | −4dp |
  | 최근에 마신 술 · 가장 자주 마신 술 | 160dp | 약 168dp | +8dp |
  | 누적 | 112dp | 약 107dp | −5dp |
  | 내 취향 제목 | 20dp | 20dp | 0 |
  | 와인·위스키·통합 선택 | 48dp 알약 | 48dp 터치 영역 안 40dp 트랙 | 배치는 같고 눈에 보이는 높이만 8dp 크다 |

  세 카드의 합(416dp → 415dp)이 거의 같아 아래 내용은 1dp 남짓만 움직인다. 다만 카드 경계가 4–8dp씩 어긋나 로딩이 끝날 때 카드 사이 선이 살짝 움직인다. 실제 높이는 기록 내용(사진 유무, 이름 줄 수)에 따라 바뀌므로 이 값은 이 단말·이 데이터 기준이다.

- **H1 해결 (2026-11-22):** 스켈레톤을 140 / 168 / 108dp로 바꾸고 같은 단말에서 다시 쟀다. 칸마다 실제 카드와의 차이가 1.3dp 이하(이번 달 0.4, 최근·자주 0.4, 누적 1.3dp)로 줄어 로딩이 끝날 때 카드 경계가 거의 움직이지 않는다. `SKL-RULE-002` 통과. 위치: `profile/ProfileScreen.kt:118`.

- **H2 결정 (2026-11-22): 스텝퍼를 켜지 않는다.** 빈칸에서 시작하는 연도는 4자리를 바로 치는 편이 빠르다. `NUM-RULE-002`는 "작은 증감이 잦을 때" 쓰라는 규칙이라 해당하지 않는 것으로 본다.

## 4. 축별 근거

### 2축 — 규칙 위반

`verify-aura-app.ps1`: 81개 파일, 오류 0, 경고 0. `verify-aura-specs.ps1`: 31개 파일, 위반 0. 두 스크립트가 보는 규칙은 11개뿐이므로 "0건"은 그 11개에 대한 결과다.

### 3축 — 렌더링 계약

5개 프로필 모두 위반 0건. 별도 기록:

- **상류 문제(U1):** 일지 검색창 입력 영역이 `19dp` (모든 프로필).
- **경고(A3):** 기준선에서 설정의 재질 라벨 2개가 ExtraSmall에서 잘렸다. 라벨을 줄인 뒤 경고 0건.

### 4축 — 용도 적합성 판정

| 화면 | 통과한 규칙 |
|---|---|
| 요약 | `BTN-RULE-001/002`, `CARD-RULE-001/002/003`, `SEG-RULE-001/002/003`, `PRG-RULE-001/002`, 진행 막대 옆 설명 문구(Do), `SKL-RULE-001` |
| 일지 | `SRCH-RULE-001/002/003`, `MENU-001/002/003`, `IBTN-RULE-001/002`, `BDG-RULE-003`, `CARD-RULE-001/002/003`, `BTN-RULE-001` |
| 기록하기 | `BTN-RULE-001/003`, `FAB-DOCK-RULE-001`, 비활성 저장 사유 표시(Do), 폼 안 저장 버튼 없음(Do), `DLG-RULE-001/002/003`, `INP-RULE-001/004`, `NUM-RULE-001`, `RAT-RULE-001/002`, 별점 제목(Do), `SEG-RULE-002`, `CHIP-RULE-001`, `CARD-RULE-002`, `CARD-RULE-003`(개선 후) — `SEG-RULE-001`은 U2(Spec 공백)로 분모에서 뺌 |
| 기록 상세·고치기 | `BTN-RULE-001/003`, `DLG-RULE-001/002/003`, `RAT-RULE-002`, `BDG-RULE-001`, `DIV-RULE-001`, `FAB-DOCK-RULE-001`, 독에서 삭제하지 않음(When NOT) |
| 설정 | `BTN-RULE-001`, `CARD-RULE-001/003`, `SEG-RULE-001/003`, `DIV-RULE-001`, `FGS-RULE-002`, `SEG-RULE-002`(개선 후) |
| 공통 크롬 | `TBAR-RULE-001/002/003/004`, `IBTN-RULE-001/002`, `BISL-RULE-001/002/003`, `FGS-RULE-001/002`, `SNK-RULE-003` |

## 5. 다시 재는 법

1. `powershell -ExecutionPolicy Bypass -File .agents\skills\aura-design-system\scripts\verify-aura-app.ps1 -TargetAppDir . -FailOnViolation`
2. `powershell -ExecutionPolicy Bypass -File scripts\verify-aura-specs.ps1 -TargetAppDir . -FailOnViolation`
3. `.\gradlew.bat :app:testDebugUnitTest --tests "*AuraRenderContractTest*"` — 결과의 `RENDER` 줄을 읽는다.
4. 1축과 4축은 이 문서의 표를 같은 방법으로 다시 채우고, 날짜를 붙인 새 절(`## 2. 점수표 (YYYY-MM-DD, Aura x.y.z)`)로 남긴다.
