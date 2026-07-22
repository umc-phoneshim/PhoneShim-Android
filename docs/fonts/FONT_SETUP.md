# 폰트 리소스

폰쉼 디자인 시스템은 다음 서체를 사용합니다.

- **Pretendard** (한글): https://github.com/orioncactus/pretendard
- **Inter** (영문/숫자): https://fonts.google.com/specimen/Inter

## 추가 방법

1. 아래 파일들을 이 디렉토리에 배치 (파일명은 소문자·snake_case)
   - `pretendard_regular.ttf`, `pretendard_medium.ttf`, `pretendard_semibold.ttf`, `pretendard_bold.ttf`
   - `inter_regular.ttf`, `inter_medium.ttf`, `inter_semibold.ttf`, `inter_bold.ttf`
2. `ui/theme/Font.kt` 의 `Pretendard` / `Inter` `FontFamily` 를 실제 리소스로 교체

> 폰트 파일은 저작권 정책상 저장소에 커밋하지 않습니다. (`.ttf` 는 `.gitignore` 대상은 아니지만, 배포 라이선스를 확인 후 추가하세요.)
