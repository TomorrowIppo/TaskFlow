# TaskFlow (OOP Team Project)

## 1. 🚀 프로젝트 개요 및 아키텍처

TaskFlow 프로젝트는 최신 Android 개발 표준을 따르는 Single Activity Architecture (SAA) 기반의 MVVM 구조로 설계되었습니다.

* 주요 기술 스택: Kotlin, Jetpack Compose, Firebase
* 핵심 아키텍처: SAA (Single Activity) + MVVM
* 주요 원칙: 책임 분리 (Separation of Concerns)를 통해 유지보수성과 테스트 용이성을 확보합니다.

| 레이어 | 역할 | 담당 파일 (예) |
| :--- | :--- | :--- |
| View (UI) | 화면 렌더링 및 사용자 이벤트 수신 | AuthScreen.kt, GroupScreen.kt |
| ViewModel | 비즈니스 로직 및 상태 관리 | AuthViewModel.kt, GroupViewModel.kt |
| Repository/Model | 데이터 접근 및 Firebase 통신 | AuthRepository.kt, TaskModel.kt |

---

## 2. 🧭 Navigation 구조 및 규칙

앱의 모든 화면 전환은 MainActivity.kt 안에 정의된 NavHost를 통해 관리됩니다. 팀원은 NavController를 직접 다루지 않고, PM이 정의한 () -> Unit 타입의 람다 함수(액션)를 호출해야 합니다.

### A. 경로 상수 (ROUTE) 정의

모든 화면은 MainActivity.kt 파일에 정의된 const val ROUTE_... 상수를 통해 접근해야 합니다.

| 상수 이름 | 설명 | 예시 |
| :--- | :--- | :--- |
| ROUTE_ONBOARDING | 앱 최초 시작 화면 | FirstScreen |
| ROUTE_LOGIN | 로그인 및 인증 처리 화면 | AuthScreen |
| ROUTE_MAIN | Task 목록을 보여주는 메인 화면 | MainTaskScreen |
| ROUTE_TASK_DETAIL | Task 상세 정보 화면 (매개변수 필요) | task_detail/{taskId} |

### B. 화면 전환 요청 (팀원의 역할)

팀원이 만든 Composable 함수 내부에서 화면 전환이 필요할 때는, 함수 정의 시 인자로 받은 onNavigateTo... 람다 함수를 호출해야 합니다.

// ✅ 올바른 예시: PM이 정의한 계약(Action) 이행
Button(onClick = { onNavigateToMain() }) {
Text("로그인 완료")
}

---

## 3. ✍️ 개발 및 협업 규칙

### A. 기능 분리 및 파일 명명

기능별로 파일을 분리하여 개발합니다.

* View (UI): [기능이름]Screen.kt (예: GroupScreen.kt)
* ViewModel: [기능이름]ViewModel.kt (예: GroupViewModel.kt)

### B. Composable 함수의 Contract (계약)

모든 Composable 함수는 다음 규칙을 반드시 따라야 합니다.

1. 데이터는 외부에서 주입: ViewModel 또는 필요한 데이터(State)는 함수의 인자로 받습니다. (예: viewModel: GroupViewModel)
2. 네비게이션은 외부로 위임: 화면 전환이 필요할 경우, PM이 정의한 onNavigateTo...: () -> Unit 형태의 람다를 인자로 받아 호출합니다.

### C. 브랜치 전략

* develop: 메인 개발 브랜치. 직접 커밋 금지.
* feature/[기능이름]: 팀원들이 각자 맡은 기능을 구현하는 브랜치. (예: feature/group-management)
    * 기능 완료 시, develop 브랜치로 Pull Request (PR)를 요청하고 팀원 1명 이상의 리뷰를 거쳐 merge 합니다.

---

## 4. 🧪 테스트 및 디버깅 환경

### A. @Preview 활용 (기본)

* UI를 구현하는 팀원은 Composable 함수 위에 @Preview 어노테이션을 붙여 Android Studio의 디자인 탭에서 실시간으로 UI를 확인합니다.

### B. TestActivity.kt 활용 (격리 테스트)

* ViewModel 연동, Context 접근 등 복잡한 통합 테스트가 필요할 경우, TestActivity.kt 파일을 생성하여 해당 Composable만 호출해 격리된 테스트를 진행합니다. 이 파일은 MainActivity의 복잡한 Navigation 로직을 무시합니다.


---

## 5. 💻 GitHub 협업 및 Git 규칙

팀 프로젝트의 효율적인 코드 관리를 위해 아래 규칙을 철저히 준수합니다.

### A. 브랜치 전략 (Branching Strategy)

* main (혹은 develop): 모든 최종 코드가 merge되는 메인 브랜치입니다. 이 브랜치에는 절대 직접 커밋하지 않습니다.
* feature/[기능명]: 새로운 기능 개발 시 사용합니다. (예: feature/auth-login, feature/group-crud)
* refactor/[내용]: 대규모 코드 구조 변경 시 사용합니다. (PM 담당)

### B. 커밋 메시지 컨벤션 (Commit Message Convention)

커밋 메시지는 [타입]: [제목] 형식으로 작성하여 커밋의 목적을 명확히 합니다.

| 타입 (필수) | 설명 | 예시 |
| :--- | :--- | :--- |
| feat | 새로운 기능 추가 (Feature) | feat: 그룹 생성 화면 UI 및 라우트 추가 |
| fix | 버그 수정 (Bug Fix) | fix: 익명 로그인 시 onNavigateToMain 누락 수정 |
| refactor | 코드 리팩토링 및 구조 변경 (기능 변경 없음) | refactor: FirstScreen을 AuthScreen.kt로 분리 |
| style | 코드 포맷팅, 세미콜론 등 (로직 변경 없음) | style: Compose Modifier 순서 통일 |
| docs | 문서 수정 (README, 주석 등) | docs: README.md에 커밋 규칙 추가 |

### C. Pull Request (PR) 규칙

1.  PR 요청: feature 브랜치에서 작업 완료 후 반드시 'develop' 브랜치로 PR을 요청합니다.
2.  PR 제목: 커밋과 동일하게 [타입]: [내용] 형식을 따릅니다.
3.  리뷰: 최소 1명 이상의 팀원에게 리뷰를 요청합니다. 리뷰어의 승인 없이는 절대 merge하지 않습니다.
4.  Self-Merge 금지: 본인이 작성한 PR은 본인이 직접 merge하지 않습니다. (PM 승인 후 merge)
5.  브랜치 삭제: merge가 완료된 feature 브랜치는 즉시 삭제합니다.

---