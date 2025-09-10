# Routine-IT 성능테스트 요구사항 및 시나리오

## 🎯 성능 목표 (SLA)

### 응답시간 목표
- **평균 응답시간**: 500ms 이하
- **95%tile 응답시간**: 2초 이하
- **99%tile 응답시간**: 5초 이하
- **최대 허용 응답시간**: 10초

### 처리량 목표
- **최소 TPS**: 1,000 TPS
- **목표 TPS**: 2,000 TPS
- **최대 TPS**: 5,000 TPS

### 안정성 목표
- **에러율**: 1% 이하
- **가용성**: 99.9% 이상
- **동시 사용자**: 10,000명 지원

### 리소스 사용률 목표
- **CPU 사용률**: 70% 이하
- **메모리 사용률**: 80% 이하
- **디스크 I/O**: 90% 이하
- **네트워크 대역폭**: 90% 이하

## 📊 테스트 시나리오 설계

### 시나리오 1: 기본 사용자 여정 (30분, 1000 스레드)
**목적**: 일반적인 사용자 행동 패턴 시뮬레이션
**부하**: 동시사용자 1000명, 램프업 5분, 지속시간 30분

**플로우**:
1. 카카오 로그인 (POST /auth/kakao)
2. 내 프로필 조회 (GET /users/me)
3. 개인 루틴 목록 조회 (GET /routines/personal)
4. 그룹 목록 조회 (GET /groups)
5. 랭킹 조회 (GET /rankings/personal)
6. 알림 확인 (GET /notifications)
7. 로그아웃 (POST /auth/logout)

### 시나리오 2: 루틴 완료 집중 테스트 (20분, 2000 스레드)
**목적**: 루틴 완료 기능의 대량 동시 처리 테스트
**부하**: 동시사용자 2000명, 램프업 3분, 지속시간 20분

**플로우**:
1. 로그인
2. 개인 루틴 완료 (POST /routines/personal/:id/complete)
3. 사용자 활동 생성 (POST /user-activities/create)
4. 점수 업데이트 (POST /rankings/update-score)
5. 출석 확인 (GET /users/me/attendance/monthly)

### 시나리오 3: 채팅 시스템 스트레스 테스트 (15분, 3000 스레드)
**목적**: 실시간 채팅 시스템의 극한 부하 테스트
**부하**: 동시사용자 3000명, 램프업 2분, 지속시간 15분

**플로우**:
1. 로그인
2. 그룹 참가 (POST /groups/:id/join)
3. 연속 채팅 전송 (POST /groups/:id/chats) - 초당 5회
4. 채팅 조회 (GET /groups/:id/chats) - 초당 2회

### 시나리오 4: 대용량 데이터 조회 테스트 (25분, 1500 스레드)
**목적**: 수백만 건 데이터 환경에서의 조회 성능 테스트
**부하**: 동시사용자 1500명, 램프업 5분, 지속시간 25분

**플로우**:
1. 로그인
2. 페이징된 활동 내역 조회 (GET /user-activities/day)
3. 랭킹 조회 (GET /rankings/groups/global)
4. 타 사용자 프로필 조회 (GET /users/:id)
5. 그룹 멤버 조회 (GET /groups/joined)

## 🎭 테스트 단계별 실행 계획

### Phase 1: 기능 검증 테스트 (Smoke Test)
- **목적**: 기본 기능 동작 확인
- **부하**: 10명 동시사용자, 5분간
- **성공 기준**: 에러율 0%, 모든 API 정상 응답

### Phase 2: 부하 테스트 (Load Test)
- **목적**: 목표 부하에서의 성능 측정
- **부하**: 1000명 동시사용자, 30분간
- **성공 기준**: SLA 목표 달성

### Phase 3: 스트레스 테스트 (Stress Test)
- **목적**: 한계점까지 부하 증가
- **부하**: 100명씩 증가하여 최대 5000명까지
- **성공 기준**: 시스템 붕괴 없이 degradation

### Phase 4: 내구성 테스트 (Endurance Test)
- **목적**: 장시간 안정성 확인
- **부하**: 1000명 동시사용자, 2시간간
- **성공 기준**: 메모리 누수 없음, 성능 저하 없음

## 📈 성능 측정 지표

### 응답시간 지표
- Average Response Time
- Median Response Time
- 90th Percentile
- 95th Percentile
- 99th Percentile
- Maximum Response Time

### 처리량 지표
- Transactions Per Second (TPS)
- Requests Per Second (RPS)
- Throughput (bytes/sec)

### 에러 지표
- Error Rate (%)
- Error Count by Type
- Failed Requests Ratio

### 시스템 지표
- CPU Utilization (%)
- Memory Utilization (%)
- Disk I/O (IOPS)
- Network I/O (Mbps)
- Database Connection Pool
- Thread Pool Status