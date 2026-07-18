# 화면별 공통 UI 컴포넌트 재사용 가이드

- Figma: [폰쉼 Design Pages - Prototype](https://www.figma.com/design/jkpXsoAOUUuTvPZnq4QjcW/%ED%8F%B0%EC%89%BC-Design-Pages?node-id=1-4&p=f&m=dev)
- 대상 코드: [`app/src/main/java/com/phoneshim/android/ui/common`](../app/src/main/java/com/phoneshim/android/ui/common)
- 분석 기준: Figma `Prototype` 페이지의 화면, 상태 변형, 독립 팝업을 현재 Jetpack Compose 공통 컴포넌트와 대조했다.
- 제외 범위: Android 시스템 `Building Blocks/status-bar`, `Building Blocks/navigation`은 앱 공통 컴포넌트 후보에서 제외한다.

## 분류 범례

| 판정 | 의미 |
| --- | --- |
| **기존 재사용** | 현재 `ui/common` API로 바로 표현할 수 있다. |
| **기존 확장** | 기존 컴포넌트를 유지하고 크기, 슬롯, 상태 같은 API만 확장하는 편이 적합하다. |
| **신규 후보** | 둘 이상의 화면에서 반복되므로 `ui/common`으로 추출할 가치가 있다. |
| **화면 전용** | 한 화면의 콘텐츠나 배치에 강하게 결합되어 화면 내부 컴포넌트로 두는 편이 적합하다. |

Figma 인스턴스 이름은 원문을 병기한다. 예를 들어 Figma의 `Chekbox`는 코드의 `Checkbox`에 대응한다. 단순히 모양이 비슷한 경우에는 기존 구현으로 단정하지 않고 **기존 확장** 또는 **신규 후보**로 분류했다.

## 현재 `ui/common` 컴포넌트

| Compose API | 주요 파라미터/variant | Figma 대응 | 사용 판단 |
| --- | --- | --- | --- |
| `PrimaryButton` | `text`, `onClick`, `enabled`, `PhoneShimButtonSize(Large/Medium/Small)`, 너비·색상·shape·label style·padding | `Primary / Button` | 01을 제외한 대부분의 주요 CTA에 재사용. 56/42/36dp 크기와 화면별 스타일을 지원 |
| `SecondaryButton` | `text`, `onClick`, `enabled`, `PhoneShimButtonSize`, 너비·container/accent 색상·shape·label style·padding | `Secondary / Button` | 목표 설정·리마인더의 보조 CTA에 재사용. Primary와 동일한 크기 체계를 공유 |
| `IconButton` | `label`, drawable `icon`, 배경/콘텐츠 색상, 아이콘 폭 | 로그인 소셜 버튼과 유사 | 로그인 버튼에 쓰려면 높이, 테두리, 로고 정렬 variant 확장 필요 |
| `TopAppBar` | `title`, `titleStyle`, `navigationIcon`, `actions` 슬롯 | `upper bar` | 기본/뒤로가기/닫기/메인/MY variant를 슬롯으로 표현 가능. Figma 48px와 코드 56px 차이 확인 필요 |
| `BottomBar` | `selectedTab`, `onTabSelected` | `Bottom bar` | 메인·리마인더·리포트·마이페이지·설정에서 재사용 |
| `BottomBarTab` | `MAIN`, `REMINDER`, `REPORT` | `Main`, `Reminder`, `Report` | 하단 탭 모델. 마이페이지는 상단 `My` 액션이므로 탭 추가 대상이 아님 |
| `TextField` | value, placeholder, error, enabled, keyboard/visual transformation | `Text Field` | 시간 입력, 마이페이지 정보 입력에 재사용. 숫자/단위 suffix와 다중 행 variant는 확장 필요 |
| `Checkbox` | checked, enabled | `Chekbox` | 앱 및 사용 이유 선택에 재사용. `Mini checkbox`는 별도 크기 variant 필요 |
| `Toggle` | checked, enabled | `Toggle` | 목표 제한 설정에 재사용. 디자인의 on 색상과 현재 `error` 색상 일치 여부 확인 필요 |
| `PhoneShimIcon` | `PhoneShimIconType`, description, tint | `Goal`, `My`, `Main`, `목표 입력` 등 일부 | 제공 enum에 해당하는 아이콘은 재사용. 편집·더보기·추가 등은 enum 확장 필요 |
| `LoadingIndicator` | modifier | 직접 대응 없음 | 로딩 상태가 명시되지 않은 Prototype 화면에는 매핑 없음. 비동기 화면의 제품 로딩 정책 확정 후 사용 |
| `SelectableChip`, `SelectionField`, `SelectionDropdown` | selected/enabled, outlined/filled variant, options, 선택 callback | `Context box`, 연령 선택 팝업 | 04-1과 09의 성별·연령 선택에 공통 사용 |
| `TodoRow` | title, timeRange, plain/card variant, leading/trailing slot | `Todo List` | 05 메인과 06 리마인더에서 공통 사용 |
| `AppInfoRow` | appName, icon/supporting/trailing slot | 앱 정보·목표 행 | 04 앱 선택·목표 설정과 09 설정에서 feature wrapper의 기반으로 사용 |
| `DurationDisplay` | totalMinutes, default/compact variant, label slot | 시간/분 표시 | 04 목표 설정과 09 설정에서 공통 사용 |
| `DateNavigator`, `CalendarGrid`, `CalendarDayCell` | 표시 월, 오늘·선택 날짜, enabled, 이동·선택 callback | `Local Calendar grid`, `Date`, `Side Button` | 06 달력과 07 리포트 날짜 이동 primitive로 공통 사용 |
| `SectionCard`, `SectionHeader` | surface/border/shape/padding, title 및 content slot | 공통 카드·섹션 제목 | 04 목표 설정, 05 메인, 07 리포트 카드에 공통 사용 |
| `PhoneShimDialog`, `ConfirmationDialog` | width/shape/padding/dismiss 정책, content slot, destructive action | 권한·입력·탈퇴 팝업 | 03, 04, 08, 09의 팝업 컨테이너와 확인 동작에 공통 사용 |
| `TextInputDialog`, `PermissionNoticeItem` | 입력 상태·저장 callback, 권한 제목·설명 | 목표 작성, 권한 안내 | 04-4/09 목표 작성과 auth/setgoal 권한 안내 중복 제거 |
| `BottomMessage` | `Info/Warning/Error`, message | `Bottom Popup` | 상태 노출 primitive 구현 완료. 현재 Toast 기반 화면은 상태 소유 정책 확정 후 교체 |

## 공통화 최종 결정

화면별 표의 **신규 후보** 가운데 아래 분류를 최종 기준으로 사용한다. `ui/common`에는 feature ViewModel이나 UI 모델을 받지 않는 primitive만 두고, feature 의미가 필요한 컴포넌트는 공통 primitive를 조합하는 wrapper로 유지한다.

| 최종 분류 | 컴포넌트 | 재사용 근거 및 구현 원칙 |
| --- | --- | --- |
| **ui/common 공통화** | `CalendarGrid`, `CalendarDayCell`, `DateNavigator` | 06 리마인더와 07 리포트. 날짜 계산·선택 callback만 받고 report/reminder 상태를 참조하지 않는다. |
| **ui/common 공통화** | `TodoRow` | 05 오늘 할 일과 06 할 일 목록. 수정·드래그·완료는 slot으로 조합한다. |
| **ui/common 공통화** | `SelectableChip`, `SelectionField`, `SelectionDropdown` | 04-1과 09 사용자 정보 선택. selected/enabled 및 filled/outlined 상태를 지원한다. |
| **ui/common 공통화** | `DurationDisplay` | 04-3~04-6과 09 목표 시간. 분 단위 입력을 일관된 시간·분 문자열로 표현한다. |
| **ui/common 공통화** | `AppInfoRow` | 04 앱 선택·앱 목표와 09 앱 목표. `AppGoalRow`, `SelectableAppRow`는 feature wrapper로 둔다. |
| **ui/common 공통화** | `SectionCard`, `SectionHeader` | 04 설정 카드, 05 섹션 제목, 07 리포트 카드. 콘텐츠와 의미는 slot으로 분리한다. |
| **ui/common 공통화** | `PhoneShimDialog`, `ConfirmationDialog` | 03 권한, 04 입력, 08 탈퇴, 09 편집. dismiss 정책과 action은 호출 화면이 결정한다. |
| **ui/common 공통화** | `TextInputDialog`, `PermissionNoticeItem` | 04-4/09 목표 작성과 auth/setgoal 권한 안내의 동일 구조를 통합한다. |
| **ui/common 공통화** | `BottomMessage` | 04-1~04-4의 안내 UI. 노출 상태와 시간은 화면/ViewModel이 소유한다. |
| **feature 내부 공통화** | `GoalSetupStepIndicator`, `SetGoalBottomButtons` | 목표 설정 wizard에서만 반복되므로 setgoal/component에 유지한다. |
| **feature 내부 공통화** | `TodoEditorDialog` | 리마인더 생성·수정 정책과 상태에 결합되어 reminder/component에 유지한다. |
| **feature 내부 공통화** | `ReportTabRow`, `TimetableCell`, `SuggestionCard` 및 차트 | report 화면군에서만 재사용하며 report/component에 유지한다. |
| **feature 내부 공통화** | `GoalReachedPanel`, `AllowedAppShortcut` | 제한 상태와 허용 앱 정책에 결합되어 appblocking/component에 유지한다. |
| **화면 전용** | `GoalProgressCard` | 메인의 진행 데이터와 표현에 결합되므로 main 내부에 유지한다. |
| **기존 Material 사용** | `TextAction`, `PopupMenu` | 각각 `TextButton`, `DropdownMenu`로 표현하고 별도 wrapper를 만들지 않는다. |

`BottomMessage`는 공통 primitive와 Preview까지 제공하지만 현재 setgoal 검증이 Android `Toast`로 직접 처리된다. 화면 상태와 노출 시간을 변경하지 않는 이번 공통화 범위에서는 Toast를 강제로 교체하지 않으며, validation message가 UI state로 승격될 때 연결한다.

## 01. 앱 클릭 직후

**Figma:** `01. 앱 클릭 직후` (`6:2308`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 전체 스플래시 영역 | 없음 | **화면 전용** | `SplashScreenContent`는 해당 feature 내부에 유지 | 로고/캐릭터와 전환 타이밍이 화면 자체의 책임이다. |
| 초기 데이터 로딩 | `LoadingIndicator` | **기존 재사용** | 없음 | 디자인에 직접 노출되지는 않으므로 실제 로딩이 필요한 경우에만 사용한다. |

## 02. 로그인

**Figma:** `02. 로그인` (`6:2276`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 브랜드 캐릭터·로고·문구 | 없음 | **화면 전용** | `LoginBrandHeader`를 feature 내부에 구성 | 다른 화면에서 반복되는 공통 앱 바가 아니다. |
| 구글/카카오 로그인 버튼 | `IconButton` | **기존 확장** | `IconButton`에 고정 높이, border, leading logo 정렬 variant | 플랫폼 로고는 `PhoneShimIcon`의 tintable 아이콘으로 취급하지 않는다. |
| 이용약관·개인정보 안내 | 없음 | **화면 전용** | 없음 | 링크가 필요하면 `AnnotatedString` 또는 별도 클릭 영역으로 구현한다. |

## 03. 접근 권한 허용

**Figma:** `03. 접근 권한 허용` (`333:2239`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 안내 캐릭터·헤드라인 | 없음 | **화면 전용** | `GuideHero` 후보는 04 시작 화면과 함께 검토 | 04 시작 화면과 레이아웃은 유사하지만 문구와 이미지 정책을 먼저 확인한다. |
| 개인정보/앱 사용 권한 안내 블록 | 없음 | **신규 후보** | `PermissionNoticeCard` | 동일 구조의 제목+설명 블록이 한 팝업 안에서 반복된다. 향후 권한 항목도 리스트로 받을 수 있게 한다. |
| 권한 동의 팝업 컨테이너 | 없음 | **신규 후보** | `PhoneShimDialog` | 04 작성 팝업, 08 탈퇴 팝업, 10 제한 안내와 공통 surface/padding 정책을 공유한다. |
| 동의 CTA (`Primary / Button`) | `PrimaryButton` | **기존 확장** | 42px `Medium` size | 현재 `Large=56`, `Small=36` 사이 variant가 필요하다. |

## 04. 목표 설정

### 04. 목표 설정 시작

**Figma:** `04. 목표 설정 시작` (`7:54`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 캐릭터와 시작 안내 | 없음 | **화면 전용** | 없음 | 목표 설정 진입 콘텐츠로 유지한다. |
| 시작 버튼 | `PrimaryButton` | **기존 재사용** | 없음 | Figma의 56px CTA는 `Large`에 대응한다. |
| 나중에 설정하기 | 없음 | **신규 후보** | `TextAction` | 03/04 안내 흐름과 팝업 취소 동작에서 텍스트형 보조 액션으로 재사용 가능하다. |

### 04-1. 성별/나이 선택

**Figma:** `04-1. 성별/나이선택` (`275:972`), 선택 상태 (`275:1207`), 누락 상태 (`275:1284`), `나이 팝업` (`275:1245`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 뒤로가기 상단 바 (`upper bar`, `back`) | `TopAppBar`, `PhoneShimIcon(ChevronLeft)` | **기존 재사용** | 없음 | 제목이 비어도 navigation slot을 유지한다. |
| 1~4단계 표시 | 없음 | **신규 후보** | `GoalSetupStepIndicator(currentStep, totalSteps)` | 04-1~04-5 전 화면에서 동일한 원·연결선 구조가 반복된다. |
| 성별 선택 chip (`Context box`) | 없음 | **신규 후보** | `SelectableChip` | 09 사용자 정보 chip에도 재사용한다. selected/enabled 상태를 제공한다. |
| 나이 선택 필드와 화살표 | `PhoneShimIcon(ChevronRight)` 일부 | **신규 후보** | `SelectionField` | 09 연령 변경과 동일한 값+드롭다운 트리거 구조로 사용한다. |
| 연령 목록과 `Mini checkbox` | `Checkbox` | **기존 확장** | `CheckboxSize.Mini(12dp)`, `SelectionDropdown` | 표준 24dp와 mini 12dp의 터치 영역은 시각 크기와 별도로 최소 크기를 보장한다. |
| 누락 오류 메시지/Bottom Popup | 없음 | **신규 후보** | `BottomMessage(type, text)` | 04-1~04-4의 누락·유효성 오류에서 공통 사용한다. |
| 다음 버튼 | `PrimaryButton` | **기존 확장** | 42px `Medium` size | disabled 색상도 Figma 상태와 비교한다. |

### 04-2. 앱 선택

**Figma:** `04-2. 어플 선택` (`7:141`), 누락 상태 (`389:1057`), `04-2. Bottom Popup` (`466:5211`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 상단 바·단계 표시 | `TopAppBar` + 신규 `GoalSetupStepIndicator` | **기존 재사용 + 신규 후보** | 단계 값 2 | 04 전체 흐름과 같은 구조를 사용한다. |
| 앱 선택 행과 `Chekbox` | `Checkbox` | **신규 후보** | `SelectableAppRow` | 앱 아이콘·이름·선택 상태를 받고 04-4/05/09의 앱 행과 데이터 모델을 공유한다. |
| 앱 추가 (`plus button`) | `PhoneShimIcon` 일부 | **기존 확장** | `PhoneShimIconType.Plus`, icon-only action | 아이콘의 클릭 semantics를 별도 버튼 wrapper로 제공한다. |
| 이전/다음 버튼 | `SecondaryButton`, `PrimaryButton` | **기존 확장** | 공통 `Medium` size | 좌우 배치는 화면 레이아웃에서 담당한다. |
| 미선택 안내 | 없음 | **신규 후보** | `BottomMessage` | 04-1/04-3/04-4 오류와 동일 계열이다. |

### 04-3. 전체 목표 사용 시간

**Figma:** 기본 (`275:1448`), 제한 알림 팝업 (`12:330`), 누락 상태 (`275:1722`), `04-3. Bottom Popup` (`389:1082`, `389:1088`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 시간/분 입력 (`Text Field`) | `TextField` | **기존 확장** | 숫자 keyboard, 단위 suffix, 고정 폭 variant | 값 검증은 화면 state에 두고 `errorMessage`로 표시한다. |
| 시간 요약 표시 | 없음 | **신규 후보** | `DurationDisplay` | 04-3, 04-4, 04-5, 05, 09에서 시간/분 포맷이 반복된다. |
| 제한 알림 설정 (`Toggle`) | `Toggle` | **기존 재사용** | 색상 token 점검 | label과 설명은 화면에서 조합한다. |
| 제한 알림 확인 팝업 | 없음 | **신규 후보** | `PhoneShimDialog` | 제목·본문·버튼 슬롯을 사용한다. |
| 누락 안내 | 없음 | **신규 후보** | `BottomMessage` | validation type을 error/warning/info로 구분할 수 있게 한다. |
| 이전/다음 CTA | `SecondaryButton`, `PrimaryButton` | **기존 확장** | `Medium` size | 04-2와 동일하다. |

### 04-4. 앱별 접근 횟수·목표 시간

**Figma:** 기본 (`12:499`), 접근 제한 클릭 (`275:1843`), 10분 미만 (`275:2060`), 사용자 작성 팝업 (`12:1154`), `04-4. Bottom Popup` (`389:1092`, `389:1096`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 앱 아이콘·이름·목표 값 행 | `PhoneShimIcon` 일부 | **신규 후보** | `AppGoalRow` | 04-2의 앱 기본 정보, 04-5 확인, 05 현황, 09 설정에서 공통 사용한다. trailing action slot을 둔다. |
| 접근 제한/목표 입력 아이콘 | `PhoneShimIcon` 일부 | **기존 확장** | 아이콘 enum 및 icon action wrapper | Figma `접근 제한`, `목표 입력`을 각각 명시적인 content description과 연결한다. |
| 접근 횟수/시간 입력 팝업 | `TextField`, `PrimaryButton`, `SecondaryButton` | **신규 후보** | `AppGoalEditorDialog` | 컨테이너는 `PhoneShimDialog`, 입력은 확장된 `TextField`를 재사용한다. |
| 사용자 작성 목표 입력 팝업 | `TextField`, `PrimaryButton` | **신규 후보** | `TextInputDialog` | 09의 앱 사용 목표 작성 팝업(`466:5217`)과 동일한 API를 사용한다. |
| 10분 미만 및 누락 안내 | 없음 | **신규 후보** | `BottomMessage` | 메시지와 노출 여부만 화면 state에서 전달한다. |

### 04-5. 최종 확인

**Figma:** `04-5. 최종 확인` (`12:1446`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 설정 요약 카드 | 없음 | **신규 후보** | `SectionCard`, `SectionHeader` | 05 메인과 09 설정 카드에도 동일 surface/title 구조를 적용한다. |
| 전체 목표 시간 | 없음 | **신규 후보** | `DurationDisplay` | 입력 UI가 아닌 읽기 전용 variant를 사용한다. |
| 앱별 목표 목록 | 없음 | **신규 후보** | `AppGoalRow` | 편집 action이 없는 read-only variant로 사용한다. |
| 이전/완료 CTA | `SecondaryButton`, `PrimaryButton` | **기존 확장** | `Medium` size | 버튼 배치는 화면 내부 책임이다. |

### 04-6. 목표 설정 완료

**Figma:** `04-6. 목표 설정 완료` (`12:1555`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 완료 캐릭터와 메시지 | 없음 | **화면 전용** | 없음 | 설정 완료 흐름에만 종속된다. |
| 목표 설정 요약 | 없음 | **신규 후보** | `DurationDisplay`, `AppGoalRow` | 04-5와 동일한 읽기 전용 표현을 재사용한다. |
| 메인으로 이동 | `PrimaryButton` | **기존 재사용** | 없음 | 56px이면 `Large`, 디자인이 42px이면 `Medium`을 사용한다. |

## 05. 메인 화면

**Figma:** 초기 설정 전 (`12:1678`), 초기 설정 후 (`12:1994`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| MY 액션이 있는 상단 바 | `TopAppBar`, `PhoneShimIcon(Person)` | **기존 재사용** | 없음 | `actions` 슬롯에 아이콘 버튼을 배치한다. |
| 하단 내비게이션 | `BottomBar`, `BottomBarTab` | **기존 재사용** | 없음 | 초기 선택은 `MAIN`이다. |
| 목표 설정 전 안내 카드 | `PrimaryButton` 일부 | **신규 후보** | `SectionCard` | 카드 컨테이너는 공통화하고 빈 상태 콘텐츠는 화면 전용으로 둔다. |
| 전체 목표 진행률/시간 | 없음 | **신규 후보** | `GoalProgressCard`, `DurationDisplay` | 09의 목표 값과 데이터를 공유하지만 진행률 표현은 메인 화면 variant다. |
| 앱별 사용 현황 | 없음 | **신규 후보** | `AppGoalRow` | progress와 남은 시간을 trailing content로 받는다. |
| 오늘 할 일 (`Todo List`) | 없음 | **신규 후보** | `TodoRow` | 06 리마인더의 동일 Figma 인스턴스와 재사용한다. |
| 헤더의 캐릭터/상태 표현 | 없음 | **화면 전용** | 없음 | 메인 상태에 결합된 시각 콘텐츠다. |

## 06. 리마인더

**Figma:** 초기 설정 전 (`12:2177`), 설정 후 (`407:2169`), `오늘 할 일 팝업 세팅 전` (`407:2637`), `선택 요일 할 일 팝업 세팅 후` (`407:2591`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 상단 바·하단 내비게이션 | `TopAppBar`, `BottomBar` | **기존 재사용** | 없음 | 선택 탭은 `REMINDER`다. |
| 달력 (`Local Calendar grid`, `Date`) | 없음 | **신규 후보** | `CalendarGrid`, `CalendarDayCell` | 리마인더와 07 데일리 리포트의 날짜 선택에 재사용한다. |
| 날짜 이동 (`Side Button`) | `PhoneShimIcon(ChevronLeft/Right)` | **기존 확장** | 공통 icon action wrapper | disabled/selected 상태를 지원한다. |
| 할 일 행 (`Todo List`) | 없음 | **신규 후보** | `TodoRow` | 05 메인의 오늘 할 일과 동일 모델을 사용한다. 완료, 수정, 삭제 상태를 슬롯으로 처리한다. |
| 추가/수정 (`Plus`, `Modify`) | `PhoneShimIcon` 일부 | **기존 확장** | `Plus`, `Edit` enum | 버튼 semantics와 최소 터치 영역을 제공한다. |
| 할 일 편집 팝업 | `TextField`, `PrimaryButton` 일부 | **신규 후보** | `TodoEditorDialog` | 설정 전/후 팝업은 동일 컴포넌트에 초기값만 다르게 전달한다. |

## 07. 데일리 리포트

**Figma:** 앱 사용 통계 (`466:4009`), 타임테이블 (`450:1869`), `Local Calendar grid` (`466:2448`), `데일리리포트 제안` (`466:2623`), `Daily Report Alarm` (`466:3992`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 상단 바·하단 내비게이션 | `TopAppBar`, `BottomBar` | **기존 재사용** | 없음 | 선택 탭은 `REPORT`다. |
| 날짜/기간 이동 | `PhoneShimIcon(ChevronLeft/Right)` 일부 | **기존 확장** | `ReportPeriodSelector` | 06 달력의 날짜 셀과 이동 버튼 primitive를 재사용한다. |
| 통계/타임테이블 선택 (`Daily Report Selection`) | 없음 | **신규 후보** | `SegmentedControl` | 두 리포트 화면의 모드만 전환한다. |
| 사용 합계 카드 (`Daily Report Sum Set`) | 없음 | **신규 후보** | `ReportSummaryCard`, `SectionCard` | 공통 카드 surface 위에 리포트 전용 내용을 조합한다. |
| 시간표 셀 (`Timetable Cell`, `Time Cell`) | 없음 | **신규 후보** | `TimetableCell`, `TimeAxisCell` | 두 리포트 화면과 타임테이블 상세에서 반복 사용한다. |
| 앱 사용 통계 차트 | 없음 | **화면 전용** | 없음 | 차트의 축·데이터 시각화는 report feature에 유지한다. |
| AI 제안 카드 | `PrimaryButton` 일부 | **신규 후보** | `SuggestionCard` | 리포트 제안과 알림 카드가 title/body/action 구성을 공유한다. |
| 목표 알림 (`Target Alarm`) | `PhoneShimIcon(Bell)` 일부 | **기존 확장** | 아이콘 enum 매핑 점검 | 알림 행은 report feature에 두고 icon primitive만 재사용한다. |

## 08. 마이페이지

**Figma:** `08. 마이페이지` (`78:1718`), `마이페이지 옵션` (`466:4943`), `탈퇴 팝업` (`78:2042`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| MY 상단 바·하단 내비게이션 | `TopAppBar`, `BottomBar` | **기존 재사용** | 없음 | 더보기 action은 `TopAppBar.actions`에 배치한다. |
| 사용자 정보 입력 (`Text Field`) | `TextField` | **기존 재사용** | 필요 시 read-only/label 슬롯 | 이름 등 일반 문자열 입력은 현재 API로 처리한다. |
| 더보기 메뉴 | 없음 | **신규 후보** | `PopupMenu`, `PopupMenuItem` | 04/09 선택 팝업과 surface primitive를 공유하되 메뉴 semantics를 유지한다. |
| 탈퇴 확인 | 없음 | **신규 후보** | `ConfirmationDialog` 또는 `PhoneShimDialog` variant | 제목, 설명, 취소/탈퇴 action과 destructive 색상을 파라미터로 받는다. |

## 09. 설정

**Figma:** `09. 설정(PREF)` (`466:4953`), `연령 선택 팝업` (`466:5044`), `앱 사용 목표 작성 팝업` (`466:5217`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 뒤로가기 상단 바·하단 내비게이션 | `TopAppBar`, `BottomBar` | **기존 재사용** | 없음 | Figma 구조대로 두 내비게이션의 동시 노출 여부를 제품 흐름에서 확인한다. |
| 사용자 정보/목표 설정 카드 | `PhoneShimIcon(Target/Person)` 일부 | **신규 후보** | `SectionCard`, `SectionHeader` | 04-5와 05 메인 카드에 재사용한다. |
| 성별/연령 chip 및 팝업 | `Checkbox` 일부 | **신규 후보 + 기존 확장** | `SelectableChip`, `SelectionField`, `SelectionDropdown`, mini size | 04-1과 동일한 선택 컴포넌트를 사용한다. |
| 전체 목표 시간 | 없음 | **신규 후보** | `DurationDisplay` | 편집 action을 trailing slot으로 제공한다. |
| 앱별 목표 목록 | `PhoneShimIcon` 일부 | **신규 후보** | `AppGoalRow` | 04-4/04-5/05와 같은 모델을 사용한다. |
| 목표 입력/접근 제한 액션 | `PhoneShimIcon` 일부 | **기존 확장** | icon enum 및 icon action wrapper | 아이콘만으로 상태가 불명확하지 않도록 접근성 라벨을 제공한다. |
| 앱 사용 목표 작성 팝업 | `TextField`, `PrimaryButton` | **신규 후보** | `TextInputDialog` | 04-4 사용자 작성 팝업과 같은 컴포넌트를 사용한다. |

## 10. 사용 제한과 사용 이유

### 10-1. 앱 사용 이유 입력

**Figma:** `10_1. 어플 사용 이유 입력` (`862:2343`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 사용 이유 선택 팝업 | 없음 | **신규 후보** | `PhoneShimDialog`, `SelectionList` | 03 권한 및 04 편집 팝업과 컨테이너를 공유한다. |
| 이유 선택 행과 `Chekbox` | `Checkbox` | **기존 재사용** | 없음 | 행 전체 클릭과 checkbox 상태를 같은 이벤트로 연결한다. |
| 확인 CTA | `PrimaryButton` | **기존 재사용** | `Small` | Figma 36px 버튼에 대응한다. |

### 10-2. 전체 폰 제한

**Figma:** 제한 (`862:2503`), 제한 이후 (`862:2554`), 목표 시간 알림 (`862:2659`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 제한/목표 달성 안내 패널 | `PrimaryButton` 일부 | **신규 후보** | `GoalReachedPanel` | 10-2와 10-3에서 제목·이미지·본문·CTA만 달라지는 공통 구조다. |
| 전화/메시지 허용 앱 | `PhoneShimIcon` 일부 | **신규 후보** | `AllowedAppShortcut` | 제한 이후 화면의 허용 action으로 유지하되 동일한 아이콘+label primitive를 쓴다. |
| 제한 이후 캐릭터·문구 | 없음 | **화면 전용** | 없음 | 전체 제한 완료 상황에만 종속된다. |
| 확인 CTA | `PrimaryButton` | **기존 재사용** | `Small` 또는 너비 정책만 modifier로 지정 | 버튼 자체에 화면 위치 정책을 넣지 않는다. |

### 10-3. 주의 앱 제한

**Figma:** 제한 (`862:2688`), 목표 시간 알림 (`862:2755`)

| 화면 영역/UI 요소 | 대응 Compose | 판정 | 확장 또는 신규 후보 | 구현 참고 |
| --- | --- | --- | --- | --- |
| 앱별 제한/달성 안내 패널 | `PrimaryButton` 일부 | **신규 후보** | `GoalReachedPanel` | `appName`, title, illustration, message를 파라미터로 받아 10-2와 공유한다. |
| 확인 CTA | `PrimaryButton` | **기존 재사용** | `Small` | 10-2와 동일하다. |

## 신규 공통 컴포넌트 후보 우선순위

| 우선순위 | 후보 | 최소 재사용 근거 | 권장 책임 |
| --- | --- | --- | --- |
| P0 | `GoalSetupStepIndicator` | 04-1, 04-2, 04-3, 04-4, 04-5 | 현재 단계와 전체 단계 렌더링 |
| P0 | `AppGoalRow` | 04-4, 04-5, 04-6, 05, 09 | 앱 정보, 목표 시간, 상태 및 trailing action |
| P0 | `DurationDisplay` | 04-3, 04-4, 04-5, 04-6, 05, 09 | 시간/분 포맷 및 읽기 전용 표시 |
| P0 | `PhoneShimDialog` | 03, 04-3/04-4, 08, 10 | 공통 surface, padding, 제목/본문/action 슬롯 |
| P0 | `BottomMessage` | 04-1, 04-2, 04-3, 04-4 | 하단 validation/info 메시지 |
| P1 | `SectionCard` / `SectionHeader` | 04-5, 05, 07, 09 | 카드 surface와 아이콘·제목 헤더 |
| P1 | `SelectableChip` / `SelectionDropdown` | 04-1, 09 | 단일 선택 및 드롭다운 목록 |
| P1 | `TodoRow` / `TodoEditorDialog` | 05, 06 | 할 일 표시와 추가·수정 |
| P1 | `CalendarGrid` / `CalendarDayCell` | 06, 07 | 날짜 그리드와 선택 상태 |
| P1 | `TimetableCell` / `TimeAxisCell` | 07 통계, 07 타임테이블 | 리포트 시간 축과 사용 셀 |
| P1 | `GoalReachedPanel` | 10-2 제한/알림, 10-3 제한/알림 | 제한·목표 달성 안내 레이아웃 |
| P2 | `TextInputDialog` | 04-4, 09 | 제목·설명·입력·확인 action 조합 |
| P2 | `TextAction` | 04 시작, 팝업 취소/보조 동작 | 텍스트형 액션의 typography와 semantics |

## 기존 컴포넌트 권장 확장 요약

1. `PrimaryButton`과 `SecondaryButton`은 공통 `PhoneShimButtonSize`의 `Large(56dp)`, `Medium(42dp)`, `Small(36dp)`을 사용한다.
2. `Checkbox`에 mini 시각 크기 variant를 추가하되 최소 터치 영역은 유지한다.
3. `TextField`에 숫자 입력, 단위 suffix, 고정 폭, 필요 시 multi-line variant를 슬롯 기반으로 추가한다.
4. `PhoneShimIconType`에 Plus, Edit, More, Close 등 실제 반복 아이콘을 추가하고 icon-only 클릭 wrapper를 제공한다.
5. `IconButton`은 로그인에 재사용할 경우 높이, border, leading logo 정렬을 variant로 노출한다.
6. `TopAppBar`는 Figma 48px와 현재 코드 56px의 기준을 확정한 뒤 height 또는 size variant로 통일한다.

## Figma 노드 인덱스

| 흐름 | 화면/상태 | Node ID |
| --- | --- | --- |
| 01 | 앱 클릭 직후 | `6:2308` |
| 02 | 로그인 | `6:2276` |
| 03 | 접근 권한 허용 | `333:2239` |
| 04 | 목표 설정 시작 | `7:54` |
| 04-1 | 성별/나이선택 | `275:972` |
| 04-1 | 성별/나이선택2 | `275:1207` |
| 04-1 | 성별/나이선택 누락 | `275:1284` |
| 04-1 | 나이 팝업 | `275:1245` |
| 04-1 | Bottom Popup | `389:1074` |
| 04-2 | 어플 선택 | `7:141` |
| 04-2 | 어플 선택 누락 | `389:1057` |
| 04-2 | Bottom Popup | `466:5211` |
| 04-3 | 목표 사용 시간 설정 | `275:1448` |
| 04-3 | 제한 알림 팝업 | `12:330` |
| 04-3 | 목표 사용 시간 설정 누락 | `275:1722` |
| 04-3 | Bottom Popup | `389:1082`, `389:1088` |
| 04-4 | 어플 접근 횟수&목표 설정 | `12:499` |
| 04-4 | 어플 접근 제한 버튼 클릭 | `275:1843` |
| 04-4 | 10분 미만 입력 | `275:2060` |
| 04-4 | 사용자 작성 팝업 | `12:1154` |
| 04-4 | Bottom Popup | `389:1092`, `389:1096` |
| 04-5 | 최종 확인 | `12:1446` |
| 04-6 | 목표 설정 완료 | `12:1555` |
| 05 | 메인 화면 초기 설정 전 | `12:1678` |
| 05 | 메인 화면 초기 설정 후 | `12:1994` |
| 06 | 리마인더 초기 설정 전 | `12:2177` |
| 06 | 리마인더 설정 후 | `407:2169` |
| 06 | 오늘 할 일 팝업 세팅 전 | `407:2637` |
| 06 | 선택 요일 할 일 팝업 세팅 후 | `407:2591` |
| 07 | 데일리 리포트 앱 사용 통계 | `466:4009` |
| 07 | 데일리 리포트 타임테이블 | `450:1869` |
| 07 | Local Calendar grid | `466:2448` |
| 07 | 데일리리포트 제안 | `466:2623` |
| 07 | Daily Report Alarm | `466:3992` |
| 08 | 마이페이지 | `78:1718` |
| 08 | 마이페이지 옵션 | `466:4943` |
| 08 | 탈퇴 팝업 | `78:2042` |
| 09 | 설정(PREF) | `466:4953` |
| 09 | 연령 선택 팝업 | `466:5044` |
| 09 | 앱 사용 목표 작성 팝업 | `466:5217` |
| 10-1 | 어플 사용 이유 입력 | `862:2343` |
| 10-2 | 폰 전체 제한 | `862:2503` |
| 10-2 | 폰 전체 제한 이후 화면 | `862:2554` |
| 10-2 | 폰 목표 시간 알림 | `862:2659` |
| 10-3 | 주의어플 별 제한 | `862:2688` |
| 10-3 | 주의어플 별 목표 시간 알림 | `862:2755` |

Prototype 하단의 `목표 입력` component (`12:621`), `Bottom bar` component (`12:1738`), `upper bar` component set (`6:619`)은 사용자 화면이 아니라 Figma 로컬 컴포넌트 정의다. 각각 `PhoneShimIcon` 확장, `BottomBar`, `TopAppBar`의 구현 근거로 위 표에 반영했다.
