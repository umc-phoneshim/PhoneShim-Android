# Demo presentation build

## Build

```powershell
.\gradlew.bat assembleDemoDebug
```

APK는 `app/build/outputs/apk/demo/debug/`에 생성된다. demo 앱은 기존 dev/prod 앱과 별도 application id인 `com.phoneshim.android.demo`로 설치된다.

## Recommended flow

1. Splash가 끝나면 Google 또는 Kakao mock 로그인을 선택한다.
2. 목표 설정 시작 화면에서 사용정보 접근과 다른 앱 위에 표시 권한을 허용한다.
3. 실제 설치 앱을 하나 이상 선택하고 목표를 저장한다.
4. 완료 화면의 `차단 시연 시작`을 누른다.
5. 선택 앱이 실행된 뒤 실제 차단 서비스와 오버레이를 거쳐 차단 화면이 표시되는지 확인한다.
6. 차단 확인을 누르면 홈으로 이동하며 one-shot 트리거가 해제된다.

권한이 없으면 버튼이 권한 설정 흐름을 먼저 연다. 선택한 앱에 실행 가능한 런처 Activity가 없으면 안내 메시지를 표시한다.

## Reset

demo 빌드의 설정 화면에서 `시연 데이터 초기화`를 누르면 프로필 mock, 사용 이유 기록, 저장된 목표와 대기 중인 차단 트리거가 초기화된다. dev/prod 빌드에는 이 버튼과 즉시 차단 동작이 노출되지 않는다.
