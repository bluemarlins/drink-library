# UI 일관성 · 테마 설정 작업 진행 기록 (2026-11-20)

STATUS: DONE

이 파일은 작업이 중단돼도 이어서 할 수 있도록 남기는 진행 기록이다. 끝나면 STATUS를 DONE으로 바꾼다.

## 목표 (사용자 요청)

1. drink-library 설정 화면에 테마 메뉴 추가: 재질(Solid / Liquid Glass / Glass Morphism) + 색 모드(시스템 / 라이트 / 다크)
2. Liquid Glass · Glass Morphism이 Aura 리서치 문서(`aura-design-system/docs/foundation/06-materiality-and-depth.md`, 커밋 안 된 수정본 포함) 스펙대로 구현됐는지 확인
3. drink-library UI 평가, Aura 반영도 확인
4. Aura에 부족한 부분은 Aura를 개선하거나 컴포넌트를 추가 → drink-library에 반영 → 에뮬레이터(emulator-5554)에서 확인 반복
5. 화면 간 일관성: "기록하기" · "기록 고치기"에 floating 콘셉트 적용(현재 AuraScaffold + SurfaceDocked)
6. 사용자 입장에서 기능과 화면을 점검하고 개선 사항 요약 및 적용

## 발견 사항

- Glass 스펙 차이: 모드별 blur(26/28dp)와 saturation(1.35/1.22)이 하나의 값(26dp/1.2)으로 묶여 있었고, 마이크로 노이즈 그레인(1.8%/2.2%)과 다크 하이라이트 클램프가 없었다. Liquid 폴백 경로는 liquidSaturation이 아니라 Glass saturation을 쓰고 있었다.
- 요약 탭 콜드 스타트: 첫 emit 전 2~3초 동안 "이번 달 · 1월 / 0잔 / 첫 잔을 기록해 보세요"를 보여준다. 로딩 중인데 빈 상태로 보이는 문제다.
- 기록하기 / 기록 고치기: 상단에 불투명한 docked 바가 있고, 저장 버튼이 폼 끝에 인라인으로 있다. 상위 화면의 floating 콘셉트와 다르다.
- 기록 상세: 고치기 / 지우기가 긴 스크롤 맨 아래에 있고, 지우기 버튼이 위험 동작처럼 보이지 않는다.
- 기록하기 상세 단계: "더 남기기 (선택)" 링크의 왼쪽 정렬이 다른 요소와 어긋난다.

## 진행 상황

### Aura (aura-design-system, 미커밋)
- [x] AuraGlass: noiseAlpha, highlightGain 추가, Light/Dark별 blur·saturation 지정
- [x] GlassSurface: frosted grain 레이어, highlight gain, liquid 폴백에 liquidSaturation 적용
- [x] AuraGlassTest: 스펙 값 검증 추가
- [x] 신규 AuraFloatingActionBar (docs/components/actions/aura-floating-action-bar.md)
- [x] 신규 AuraSkeleton / AuraSkeletonText (docs/components/feedback/aura-skeleton.md), motion.skeletonPulseDurationMillis
- [x] TaskScreenComponentsTest
- [x] :design-system:testDebugUnitTest 통과 (59 tests; Robolectric에서 그레인 비트맵 생성 실패 → null이면 건너뛰도록 보호)
- [x] preview 앱(Feedback·Progress 화면)에 신규 컴포넌트 데모 추가
- [x] 1.3.0 버전 표기(build.gradle.kts, README, skills) + NAV-RULE-001에 작업 화면 floating 규칙 추가
- [x] aar 빌드 후 drink-library/app/libs/aura-design-system-1.3.0.aar 로 교체
- [x] FloatingGlassScaffold 상·하단 스크롤 가장자리 베일을 캡슐 줄까지 확장(기기에서 겹침 확인 후 수정)
- [x] 카테고리별 커밋 (docs/foundation 06·07, glass-surface.md의 기존 미커밋 리서치 수정본은 사용자 작업이라 커밋하지 않음)

### drink-library
- [x] 테마 설정 저장(DataStore) + MainActivity 적용 + 설정 화면 메뉴 (기기 확인)
- [x] 기록하기 · 기록 고치기 → floating + RecordSaveBar (기기 확인)
- [x] 기록 상세 → RecordEditBar, 큰 제목 중복 제거 (기기 확인)
- [x] 요약 탭 로딩 skeleton + flowOn(Default) (기기 확인)
- [x] specs/designer/design-system.md, harness.md, AuraConformanceTest 갱신
- [x] 에뮬레이터에서 4/6 조합 확인(리퀴드·라이트, 글래스모피즘·다크, 솔리드·라이트, 리퀴드·다크). 글래스모피즘·라이트, 솔리드·다크는 미확인
- [x] 콜드 스타트 빈 화면 → 화면 설정 선읽기 + 스플래시 유지
- [x] SettingsViewModelAppearanceTest 추가, testDebugUnitTest·ktlintCheck·lint 통과
- [x] task-log.md P2-20 기록, 로컬 커밋(push 안 함)
