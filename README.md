# 폰쉼 - Android

> 습관적인 스마트폰 과의존을 방지하고 장기적인 차원의 스마트폰 사용 습관 개선을 돕는 안드로이드 애플리케이션 **폰쉼**의 프론트엔드 저장소입니다.
> 

## 1. 프로젝트 소개

- **앱 이름**: 폰쉼 (Phone-Shim)
- **한 줄 설명**: "폰을 쉬다"의 의미로, 현대인들에게 스마트폰 과다 사용에 대한 경각심을 주고 예정된 스케줄을 이행할 수 있도록 돕는 스마트폰 절제 및 타이머 서비스입니다.
- **서비스 목표**: 단순한 단기적 강제 잠금을 넘어, 장기적인 관점에서 사용자가 스스로 스마트폰 사용을 통제하고 올바른 습관을 형성하도록 지원합니다.

---

## 2. 팀원 소개 및 역할 분담

| **닉네임/이름** | **파트** | **역할 분담** |
| --- | --- | --- |
| **노뱅/김호균** | Android (팀장) | 목표 설정(온보딩) · 디자인 시스템 |
| **폴/정휘** | Android | 메인 화면 |
| **너울/주연우** | Android | 로그인·회원가입 · 앱 차단 UI · 네비게이션/공통 컴포넌트 |
| **담담/임성민** | Android | 차단 엔진(백그라운드) · BaseViewModel(MVI 공통) |
| **이안/이용욱** | Android | 마이페이지 · 데일리 리포트 |
| **타로/최유진** | Android | 리마인더 · 설정(PREF) |

> 담당자는 각 화면의 `feature/*/<닉네임>` 브랜치 작업자 기준입니다.

---

## 3. 기술 스택

안드로이드 팀에서 사용하는 주요 기술 스택 및 개발 환경입니다.

| **분류** | **기술 스택** | **상세 환경 / 주요 라이브러리** |
| --- | --- | --- |
| **Language** | Kotlin | v2.0.x 기반 (Modern Android Development) |
| **UI Framework** | Jetpack Compose | 완전한 선언형 UI 프레임워크 기반 설계 |
| **Design System** | Compose Theme (M3) | Figma Variable 기반 Color/Type/Shape 토큰화 (`ui/theme`), Pretendard·Inter 서체 |
| **Architecture** | Clean Architecture + MVVM | Domain 중심 설계 및 Single Source of Truth(SSOT) 지향 |
| **Asynchronous** | Coroutines & Flow | 비동기 데이터 스트림 처리 및 상태 관리 |
| **DI** | Hilt | 의존성 주입 최적화 |
| **Network** | Retrofit2, OkHttp3 | RESTful API 통신 및 로깅 인터셉터 |
| **Realtime** | Socket.io Client | 실시간및 타이머 상태 동기화 |
| **Local DB** | Room / DataStore | 로컬 캐싱 및 유저 설정 저장 |

---

## 4. 프로젝트 폴더 구조

안드로이드의 아키텍처 확장성과 백엔드 도메인 구조(DDD)와의 싱크를 고려하여, **기능 중심(Feature-driven) 패키지 구조**에 Clean Architecture를 결합한 형태로 구성합니다.

> Figma 화면 설계([디자인 링크](https://www.figma.com/design/jkpXsoAOUUuTvPZnq4QjcW))를 기준으로, 기능별 화면 수와 복잡도에 맞춰 패키지를 세분화했습니다. `setgoal`은 목표 설정 시작 + `04-1~04-6` 단계 화면과 접근 권한 동의 팝업으로 구성해 `screen/component/viewmodel`로 분리했고, 설정(`pref`)·앱 차단 화면(`appblocking`) 패키지와 백그라운드 차단 엔진(`blocking`)을 반영했습니다.

```
.
├── app/                           # App 모듈 및 Hilt Application 설정
│   └── navigation/                # 최상위 NavHost (로그인 여부 / 초기 설정 여부 분기)
├── blocking/                      # 백그라운드 차단 엔진 (감지·판정·오버레이·알람, 포그라운드 서비스)
├── data/                          # Data 레이어 (인프라스트럭처)
│   ├── api/                       # Retrofit 인터페이스 및 네트워크 모델
│   ├── database/                  # Room DB 및 local entity
│   └── repository/                # Repository 구현체 및 DataSource 인터페이스
├── domain/                        # Domain 레이어 (Pure Kotlin / 비즈니스 규칙)
│   ├── model/                     # 비즈니스 핵심 엔티티 명세
│   ├── repository/                # Repository 인터페이스 정의
│   └── usecase/                   # 유스케이스 (예: StartTimerUseCase)
└── ui/                            # Presentation 레이어 (Jetpack Compose)
    ├── theme/                     # Colors, Typography, Theme 등 디자인 시스템
    ├── common/                    # 공통 커스텀 컴포넌트 및 베이스 구조
    └── features/                  # 폰쉼 서비스의 핵심 기능별 스크린 (01~10)
        ├── auth/                  # 01~02. 스플래시 · 로그인 · 회원가입
        ├── setgoal/               # 03~04. 접근 권한 동의 팝업 + 목표 설정(04 · 04-1~04-6)
        │   ├── screen/            #   단계별 화면 (시작/성별·나이/사용시간/앱선택/제한/확인/완료)
        │   ├── component/         #   접근 권한 동의 팝업 등 공용 컴포넌트
        │   └── viewmodel/         #   단계 간 공유 상태 (SetGoalViewModel, MVI)
        ├── main/                  # 05. 메인 화면 (초기 설정 전/후 분기)
        ├── reminder/              # 06. 리마인더 (설정 전/후, 설정 과정 팝업)
        ├── report/                # 07. 데일리 리포트 (타임테이블 · AI 제안 · 요약)
        ├── mypage/                # 08. 마이페이지 (본체 · 사이드 슬라이드 · 탈퇴 팝업)
        ├── pref/                  # 09. 설정 (PREF · 목표/제한 재설정, MVI)
        └── appblocking/           # 10. 앱 차단 · 목표 도달 화면 (blocking 엔진이 오버레이로 표시)
```

> `ui/theme/` 는 Figma Variable 을 그대로 옮긴 디자인 시스템 레이어입니다. 자세한 내용은 아래 4-1 참고.

### 4-1. 🎨 디자인 시스템 (`ui/theme/`)

Figma **"폰쉼 Design Pages"** 의 Variable(Color / Text Style) 정의를 코드 토큰으로 1:1 이관했습니다. 모든 화면은 하드코딩된 값 대신 이 토큰을 사용합니다.

| **파일** | **역할** |
| --- | --- |
| `Color.kt` | 원시 팔레트(`PhoneShimPalette`) + 역할 기반 시맨틱 컬러(`PhoneShimColors`) |
| `Type.kt` | Pretendard(한글)·Inter(영문/숫자) 텍스트 스타일 + M3 `Typography` 매핑 |
| `Font.kt` | 서체 `FontFamily` 정의 (실제 폰트 리소스 연결 지점) |
| `Shape.kt` | 라운드 스케일(4~24dp)을 M3 `Shapes` 로 매핑 |
| `Dimens.kt` | 간격·컴포넌트 사이즈 토큰 (화면 여백 16dp 기준) |
| `Theme.kt` | `PhoneShimTheme` 프로바이더 + `PhoneShimTheme.colors` 접근자 |

**컬러 토큰**

| 그룹 | 토큰 | 값 |
| --- | --- | --- |
| Primary | `Primary100 ~ 600` | `#F4F8F1` · `#DCE7D4` · `#B2C69D` · **`#8CAB7A`(메인)** · `#6D8B5E` |
| Neutral | `White ~ Gray900` | `#FFFFFF` · `#ECECEC` · `#CCCCCC` · `#888888` · `#555555` · `#262626` |
| Background | `SoftCream` / `Cream` | `#FFFDF7` · `#FCFAF2` |
| Semantic | `Error` | `#E56767` |

**타이포그래피** — 한글 `Pretendard`, 영문·숫자 `Inter`

- KOR: `H3(18/SemiBold)` · `Body L(16)` · `Body M(14)` · `Caption(12)` · `Label(10)` · `Micro(8)`
- ENG·NUM: `H1(24/Bold)` · `H2(20/SemiBold)` · `Body M(14)` · `Caption(12)` · `Label(10)`

> ⚠️ Pretendard·Inter 폰트 파일은 저작권상 저장소에 포함하지 않았습니다. `app/src/main/res/font/` 에 `.ttf` 를 추가한 뒤 `Font.kt` 의 `FontFamily` 를 연결하세요. (미연결 시 시스템 기본 서체로 폴백)

**사용 예시**

```kotlin
@Composable
fun Sample() {
    Text(
        text = "폰을 쉬다",
        style = MaterialTheme.typography.titleLarge,      // KOR H3
        color = PhoneShimTheme.colors.brandStrong,        // Primary 600
    )
}
```

---

## 5. 💻 화면 목록 & 플로우 정리

### 화면 목록

Figma 프로토타입([바로가기](https://www.figma.com/design/jkpXsoAOUUuTvPZnq4QjcW))의 `01.`~`10.` 넘버링을 기준으로 정리했습니다.

**상태 범례** — ✅ 완료(구현·연동) · 🟡 진행 중(UI 완료, 로직/데이터/MVI 리팩터 진행) · ⬜ 예정
**진행 중 PR** — #25 차단 엔진 · #26 리마인더 MVI · #27 설정 MVI · #28 로그인 로직

| **화면** | **스크린 ID** | **담당자** | **상태 · 비고** |
| --- | --- | --- | --- |
| **01. 스플래시** | `SplashScreen` | 너울 | ⬜ 예정 |
| **02. 로그인** | `LoginScreen` | 너울 | 🟡 UI 완료 · MVI 로직/테스트 진행 (PR #28) |
| **회원가입** | `SignUpScreen` | 너울 | 🟡 UI 완료 · 로직 진행 |
| **03. 접근 권한 동의(팝업)** | `PermissionConsentPopup` | 노뱅 | ✅ 완료 · 차단 권한 요청 연동 |
| **04. 목표 설정 시작** | `SetGoalStartScreen` | 노뱅 | ✅ 완료 |
| **04-1. 성별·나이 선택** | `GenderAgeSelectScreen` | 노뱅 | ✅ 완료 |
| **04-2. 목표 사용 시간 설정** | `UsageTimeSetScreen` | 노뱅 | ✅ 완료 |
| **04-3. 주의 앱 선택** | `AppSelectScreen` | 노뱅 | ✅ 완료 · 설치앱 실연동(packageName) |
| **04-4. 접근 제한 설정** | `AccessGoalSetScreen` | 노뱅 | ✅ 완료 |
| **04-5. 최종 확인** | `SetGoalConfirmScreen` | 노뱅 | ✅ 완료 |
| **04-6. 목표 설정 완료** | `SetGoalCompleteScreen` | 노뱅 | ✅ 완료 · 로컬 저장/오프라인 읽기 연동 |
| **05. 메인 화면** | `MainScreen` | 폴 | 🟡 UI 완료(mock) · VM/데이터 연동 진행 |
| **06. 리마인더** | `ReminderScreen` | 타로 | 🟡 UI 완료(mock) · MVI 리팩터 (PR #26) |
| **06. 리마인더 설정(팝업)** | `ReminderSetPopup` | 타로 | 🟡 UI 완료 |
| **07. 데일리 리포트(타임테이블)** | `TimetableScreen` | 이안 | 🟡 UI 완료(mock) |
| **07. 사용 이유 입력** | `UsageReasonInputScreen` | 이안 | 🟡 UI 완료 |
| **07. 데일리 리포트(AI 제안)** | `ReportAiSuggestScreen` | 이안 | 🟡 UI 완료 |
| **07. 데일리 리포트(요약)** | `ReportSummaryScreen` | 이안 | 🟡 UI 완료 |
| **08. 마이페이지** | `MyScreen` | 이안 | 🟡 UI 완료(mock) |
| **08. 마이페이지(사이드 슬라이드)** | `MySideMenuScreen` | 이안 | 🟡 UI 완료 |
| **08. 마이페이지(탈퇴 팝업)** | `WithdrawPopup` | 이안 | 🟡 UI 완료 |
| **09. 설정(PREF)** | `PrefScreen` | 타로 | 🟡 UI 완료(mock) · MVI (PR #27) · 저장 연동 예정 |
| **10. 앱 차단 · 목표 도달** | `GoalAchievedScreen` | 너울(UI) · 담담(엔진) | 🟡 UI 완료 · 차단 엔진 실기기 동작 (PR #25) |
| **10. 사용 이유 선택(차단 시)** | `UsageReasonSelectionScreen` | 너울 | 🟡 UI 완료 |

### 네비게이션 플로우

```
[앱 최초 실행]
       │
       ▼
[스플래시] ─(자동)─► [로그인] ◀──► [회원가입]
                        │ (인증 완료)
                        ▼
         (최초 1회) [접근 권한 동의] ─► [목표 설정 시작]
                                            │
                                            ▼
   [성별·나이] ─► [목표 사용 시간] ─► [주의 앱 선택] ─► [접근 제한 설정]
                                                          │
                                                          ▼
                                                    [최종 확인] ─► [설정 완료]
                                              │
                                              ▼
┌──────────────────────────────────────────────────────────────────────────┐
│  ▼ (하단 탭 1)                  ▼ (하단 탭 2)              ▼ (하단 탭 3)     │
│ [메인 화면]                   [리마인더]                [데일리 리포트]      │
│   │                             │                          │            │
│   ├─►(상단 설정)─►[설정(PREF)] └─►(팝업)─►[리마인더 설정]     ├─►[타임테이블] │
│   │                                                         │   └─►[사용 이유 입력]
│   └─►(상단 MY)──┐                                            ├─►[AI 제안]  │
│                 ▼                                            └─►[요약]    │
│           [마이페이지]                                                    │
│                 └─►(메뉴)─►[사이드 슬라이드]─►[탈퇴 확인 팝업]               │
└──────────────────────────────────────────────────────────────────────────┘
```

> ※ 목표 시간 초과 시 백그라운드 차단 엔진이 **[앱 차단 · 목표 도달]** 화면을 오버레이로 표시합니다.

---

## 6. 🧑‍💻 안드로이드 코드 컨벤션

### 네이밍 규칙

백엔드 및 안드로이드 코틀린 공식 스타일 가이드를 준수합니다.

| **대상** | **규칙** | **예시** |
| --- | --- | --- |
| **클래스/인터페이스** | PascalCase | `TimerViewModel`, `UserRepository` |
| **Compose 함수** | PascalCase (명사형) | `TimerButton()`, `DefaultDialog()` |
| **일반 함수 / 변수** | camelCase | `startTimer()`, `totalStudyTime` |
| **상수 (const val)** | UPPER_SNAKE_CASE | `MAX_TIMER_DURATION` |
| **패키지명** | 소문자 영어 단어 하나 | `auth`, `timer`, `repository` |
| **레이아웃 / XML 리소스** | snake_case | `bg_timer_circle.xml` |

---

## 7. Git 브랜치 및 커밋 전략 (백엔드 통합 싱크)

### 브랜치 전략 (Git Flow 기반)

- `main`: 출시 가능한 가장 안정적인 상태의 운영 브랜치입니다.
- `develop`: 다음 버전을 개발하는 통합 브랜치입니다.
- `feature/[기능명]/[작업자닉네임]`: 단위 기능을 개발하는 브랜치입니다.
    - *예시: `feature/timer-ui/yourname`, `feature/socket-connection/yourname`*

| Type | 사용 상황 | 예시 |
| --- | --- | --- |
| `feat` | 새로운 기능 추가 | `feat/3-barcode-scan` |
| `fix` | 버그 수정 | `fix/7-camera-permission` |
| `refactor` | 기능 변화 없는 코드 개선 | `refactor/15-api-client` |
| `chore` | 설정, 빌드, 패키지, 환경 작업 | `chore/12-retrofit-setting` |
| `docs` | 문서 수정 | `docs/readme-update` |
| `test` | 테스트 코드 추가/수정 | `test/20-login-validation` |
| `design` | UI 스타일, 레이아웃 수정 | `design/login-button-spacing` |

### 커밋 컨벤션

#### Commit Type

- `feat`: 새로운 기능 추가 또는 기존 기능 수정
- `fix`: 버그 수정 및 오류 해결
- `docs`: README, 문서 수정
- `typo`: 단순 오탈자 수정
- `chore`: 빌드 업무, 패키지 설정, 그래들(Gradle) 의존성 추가 외 기타 작업
- `res`: 이미지, 폰트, 애니메이션 등 안드로이드 리소스 자원 추가/수정

#### Commit Format

```
[Type]: [Title]

[Body - Optional]
```

- *예시*

```
*feat: implement Jetpack Compose circular timer view*
feat: 로그인 화면 구현
docs: README 실행 방법 추가
chore: Gradle 의존성 추가
```

---

## 8. PR 및 코드 리뷰 규칙 (Pn 룰 도입)

- **PR 조건**: 로컬 빌드 및 린트 검증 완료 후 개설하며, 최소 1명 이상의 안드로이드 파트원 리뷰 필수.
- **리뷰어 지정**: 안드로이드 팀 전체 태깅 및 피드백 수행.
- **코드 리뷰 Pn 규칙**:
    - `P1`: 반드시 반영해야 하는 사항 (릴리스 블로커, 아키텍처 오류 등)
    - `P2`: 적극적으로 고려 및 논의해야 하는 사항
    - `P3`: 가벼운 의견 제시 및 사소한 리팩토링 제안

### PR 작성  룰

PR 제목은 반드시 Conventional Commits 형식으로 작성한다.

```
type: 작업 내용
```

예시

```
feat: 바코드 스캔 화면 구현
feat: 알러지 정보 선택 화면 구현
fix: 카메라 권한 거부 시 앱 종료 문제 수정
refactor: API 클라이언트 구조 개선
chore: Android 프로젝트 초기 설정
docs: Git 협업 규칙 문서 추가
```

내용은 다음과 같이 작성한다

```
## 개요

<!-- 이번 PR에서 어떤 작업을 했는지 간단히 설명해주세요. -->

## 작업 내용(커밋로그-커밋과 커밋주소를 함께 포함하여 작성)

-
-
-

## 테스트 방법

- [ ] 앱 실행 확인
- [ ] 관련 화면 진입 확인
- [ ] 주요 버튼/입력 동작 확인
- [ ] 에러 없이 빌드되는지 확인

## 리뷰 필요

<!-- 리뷰어가 중점적으로 봐줬으면 하는 부분을 적어주세요. -->

## 관련 이슈

close #
```

---

## 9. 빌드 및 실행 방식

```bash
# 1. 저장소 클론
$ git clone https://github.com/[Your-Repository]/phone-shim-android.git

# 2. local.properties 세팅 (API Key 정보 등 입력 필요 시)
# 루트 경로에 local.properties 파일을 생성하고 필요한 보안 키를 기입하세요. (git 배포 금지)

# 3. Android Studio로 프로젝트 오픈 후 빌드 진행 (Gradle Sync)
```
