# Routine-It Spring Backend

구름톤 풀스택 13회차 구르다구르미팀의 루틴 관리 애플리케이션 Routine-It! Spring Boot 백엔드 서버

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+ (로컬 개발 시)
- MySQL 8.0+ (로컬 개발 시)

## 📁 프로젝트 구조

```
src/main/java/com/goormi/routine/
├── RoutineApplication.java          # 메인 애플리케이션 클래스
│
├── domain/                          # 도메인별 비즈니스 로직
│   ├── user/                       # 사용자 도메인
│   │   ├── controller/             
│   │   │   └── UserController.java       # 사용자 API
│   │   ├── service/                
│   │   │   └── UserService.java          # 사용자 비즈니스 로직
│   │   ├── repository/             
│   │   │   └── UserRepository.java       # 사용자 데이터 접근
│   │   ├── entity/                 
│   │   │   └── User.java                 # 사용자 엔티티
│   │   └── dto/                    
│   │       ├── UserRequest.java          # 사용자 요청 DTO
│   │       └── UserResponse.java         # 사용자 응답 DTO
│   │
│   ├── routine/                    # 루틴 도메인
│   │   ├── controller/
│   │   │   ├── RoutineController.java    # 루틴 API
│   │   │   └── RoutineTaskController.java # 루틴 작업 API
│   │   ├── service/
│   │   │   ├── RoutineService.java       # 루틴 비즈니스 로직
│   │   │   └── RoutineTaskService.java   # 루틴 작업 비즈니스 로직
│   │   ├── repository/
│   │   │   ├── RoutineRepository.java
│   │   │   └── RoutineTaskRepository.java
│   │   ├── entity/
│   │   │   ├── Routine.java              # 루틴 엔티티
│   │   │   ├── RoutineTask.java          # 루틴 작업 엔티티
│   │   │   └── RoutineHistory.java       # 루틴 이력 엔티티
│   │   └── dto/
│   │       ├── RoutineRequest.java
│   │       ├── RoutineResponse.java
│   │       └── RoutineTaskRequest.java
│   │
│   ├── auth/                       # 인증/인가 도메인
│   │   ├── controller/
│   │   │   └── AuthController.java       # 인증 API
│   │   ├── service/
│   │   │   ├── AuthService.java          # 인증 비즈니스 로직
│   │   │   └── KakaoOAuthService.java    # 카카오 OAuth 서비스
│   │   └── dto/
│   │       ├── LoginRequest.java
│   │       ├── TokenResponse.java
│   │       └── KakaoUserInfo.java
│   │
│   └── notification/               # 알림 도메인
│       ├── controller/
│       │   └── NotificationController.java
│       ├── service/
│       │   └── NotificationService.java
│       ├── repository/
│       │   └── NotificationRepository.java
│       ├── entity/
│       │   └── Notification.java
│       └── dto/
│           └── NotificationRequest.java
│
├── common/                         # 공통 모듈
│   ├── exception/                 # 예외 처리
│   │   ├── GlobalExceptionHandler.java   # 전역 예외 핸들러
│   │   ├── BusinessException.java        # 비즈니스 예외
│   │   ├── ErrorCode.java                # 에러 코드
│   │   └── CustomExceptions.java         # 커스텀 예외들
│   │
│   ├── response/                  # 공통 응답 포맷
│   │   ├── ApiResponse.java              # API 응답 래퍼
│   │   ├── ErrorResponse.java            # 에러 응답
│   │   └── PageResponse.java             # 페이징 응답
│   │
│   ├── util/                      # 유틸리티
│   │   ├── DateUtil.java                 # 날짜 유틸
│   │   ├── StringUtil.java               # 문자열 유틸
│   │   └── SecurityUtil.java             # 보안 유틸
│   │
│   ├── annotation/                # 커스텀 어노테이션
│   │   ├── CurrentUser.java              # 현재 사용자 주입
│   │   └── ValidEnum.java                # Enum 검증
│   │
│   ├── constant/                  # 상수 및 Enum
│   │   ├── RoutineStatus.java            # 루틴 상태
│   │   ├── NotificationType.java         # 알림 타입
│   │   └── UserRole.java                 # 사용자 권한
│   │
│   └── aspect/                    # AOP
│       └── LoggingAspect.java            # 로깅 AOP
│
├── config/                         # 설정 클래스
│   ├── SecurityConfig.java               # Spring Security 설정
│   ├── WebSocketConfig.java              # WebSocket 설정
│   ├── JpaConfig.java                    # JPA 설정
│   ├── CorsConfig.java                   # CORS 설정
│   ├── SwaggerConfig.java                # Swagger 설정
│   └── SchedulerConfig.java              # 스케줄러 설정
│
└── test/                          # 테스트 유틸
```

### 테스트 디렉토리 구조
```
src/test/java/com/goormi/routine/
├── domain/
│   ├── user/
│   │   ├── controller/
│   │   │   └── UserControllerTest.java
│   │   ├── service/
│   │   │   └── UserServiceTest.java
│   │   └── repository/
│   │       └── UserRepositoryTest.java
│   │
│   └── routine/
│       ├── controller/
│       │   └── RoutineControllerTest.java
│       └── service/
│           └── RoutineServiceTest.java
│
└── integration/                   # 통합 테스트
    └── RoutineIntegrationTest.java
```

## 아키텍처

### 패키지 구조 설명

#### 1. **domain/** - 도메인별 모든 레이어 포함
각 도메인(user, routine, auth, notification)마다 독립적인 구조를 가집니다:
- **controller**: REST API 엔드포인트
- **service**: 비즈니스 로직 구현
- **repository**: 데이터 접근 계층
- **entity**: JPA 엔티티
- **dto**: 요청/응답 데이터 전송 객체

#### 2. **common/** - 공통 유틸리티 및 설정
모든 도메인에서 공통으로 사용하는 클래스들:
- **exception**: 예외 처리 및 전역 핸들러
- **response**: 통일된 API 응답 포맷
- **util**: 공통 유틸리티 함수
- **annotation**: 커스텀 어노테이션
- **constant**: 공통 상수 및 Enum
- **aspect**: AOP 관련 클래스

#### 3. **config/** - 애플리케이션 설정
Spring Boot 설정 클래스들:
- Security, JPA, CORS, WebSocket 등의 설정
- 외부 라이브러리 설정

#### 4. **test/** - 테스트 지원
테스트에서만 사용하는 유틸리티 및 픽스처

### 패키지 명명 규칙

- **entity**: JPA 엔티티 클래스
- **repository**: Spring Data JPA 리포지토리
- **service**: 비즈니스 로직 서비스
- **dto**: 데이터 전송 객체
- **controller**: REST API 컨트롤러
- **config**: 설정 클래스
- **exception**: 예외 클래스
- **util**: 유틸리티 클래스

## 기술 스택

- **Framework**: Spring Boot 3.5.4
- **Language**: Java 17
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA
- **Security**: Spring Security + OAuth2 (Kakao)
- **WebSocket**: Spring WebSocket
- **Build Tool**: Gradle
- **Container**: Docker & Docker Compose

## 개발 가이드

### 브랜치 전략
- `main`: 프로덕션 배포 브랜치
- `develop`: 개발 통합 브랜치
- `feature/*`: 기능 개발 브랜치
- `hotfix/*`: 긴급 수정 브랜치

### 커밋 메시지 컨벤션
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅, 세미콜론 누락 등
refactor: 코드 리팩토링
test: 테스트 코드 추가
chore: 빌드 업무 수정, 패키지 매니저 수정
```

## API 문서

서버 실행 후 Swagger UI에서 확인:
```
http://localhost:8080/swagger-ui.html
```