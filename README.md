# 온라인 클래스 시스템


## 프로젝트 개요

온라인 강의 플랫폼의 핵심 도메인(강의, 수강신청)을 구현한 백엔드 시스템.

<br>

---

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot |
| ORM | Spring Data JPA, Hibernate |
| Database | MySQL (production), H2 (test) |
| Build | Gradle |

<br>

---

## 실행 방법

### 사전 요구사항
- JDK 21
- Docker

### 1. 애플리케이션 실행

```bash
./gradlew bootRun
```

애플리케이션 실행 시 `spring-boot-docker-compose` 설정으로 인해 `compose.yaml`의 MySQL 컨테이너가 자동으로 함께 실행됩니다.

서버 기본 포트: `8080`

### 2. 초기 테스트 데이터 적재 (선택)

API 테스트 검증을 위한 데이터가 필요하다면 애플리케이션 실행 후 `sql/testdata.sql`을 실행할 수 있습니다.

<br>

---

## 요구사항 해석 및 가정

### 1. 강의 상태 전이 (`DRAFT → OPEN → CLOSED`)

- 단방향 전이만 허용
  → 마감된 강의의 재오픈은 별도 비즈니스 정책으로 분리 필요
- **구현**: `Course.open()`은 `DRAFT` 상태에서만 호출 가능, `Course.close()`는 `OPEN` 상태에서만 호출 가능

### 2. 수강신청 상태 전이 (`PENDING → CONFIRMED → CANCELLED`)

- 다음 두 전이 모두 허용:
  - `PENDING → CANCELLED` (결제 전 사용자 취소)
  - `CONFIRMED → CANCELLED` (결제 후 사용자 취소)

  → 결제 전 단계에서 사용자가 직접 취소할 수단을 제공하는 것이 자연스럽다고 생각함

### 3. 강의 목록 조회 시 `DRAFT` 강의 노출 여부

- 초안 상태는 강사 본인만 보는 비공개 상태로 정의
  → 일반 강의 목록 조회에서 초안은 제외하고 응답할 수 있도록 가정

### 4. 페이지네이션 방식

- 현재 UI 요구사항이 없어 무한 스크롤 방식으로 가정함

<br>

---

## 설계 결정과 이유

### 1. 동시성 제어: 비관적락

- **고려한 대안**: 낙관적락, 레디스 분산락
- **선택 이유**:
  - 낙관적락: 인기 강의의 마지막 자리 경쟁 시 다수의 재시도 발생 → 처리량 저하 및 사용자 경험 악화
  - 레디스 분산락: 단일 인스턴스 환경에서 외부 인프라 추가는 오버엔지니어링이라고 판단함
  - 비관적락: 한 강의에 집중되는 동시 접근을 순차 처리. 단일 인스턴스 환경에서 가장 단순하고 신뢰 가능한 해결책

### 2. 정원 카운트 정책

강의 정원 카운트 시 `PENDING` 상태가 포함될 수 있도록 함.

**이유**:
- 신청 시점에 자리 확보 → 결제 단계에서 정원 초과로 실패하는 UX 회피
- 마지막 자리 경쟁이 신청 시점에 명확해짐

**추후 고려사항**: 결제되지 않은 PENDING이 방치되면 좀비 자리 발생 가능. 운영 환경에서는 PENDING 상태의 수강 신청에 10분 TTL을 설정하거나 스케줄러로 정리 필요.

### 3. 객체 간 참조: ID 기반

`Enrollment.courseId`를 `Long`으로 보유 (`@ManyToOne Course` 미사용).

**이유**:
- 현재 복잡하지 않은 도메인 상태로 객체 간 직접 참조가 불필요하다고 판단함
- `LazyInitializationException`, N+1 문제 등 JPA에서 발생할 수 있는 문제를 회피
- 도메인 간 결합도 최소화

### 4. 강의 목록 조회 시 정렬: OPEN 우선 + 최신순

- 기본 조회 시 모집중(`OPEN`) 강의를 먼저 보여주고, 그 뒤에 신청 마감(`CLOSED`)된 강의를 보여줄 수 있도록 함
- **이유**: 사용자 입장에서 신청 가능한 강의를 우선 노출하는 것이 자연스럽다고 판단함

<br>

---

## 미구현 / 제약사항

### 선택 구현 항목 구현 여부

| 항목 | 구현 여부 |
| --- | :---: |
| 신청 내역 페이지네이션 | ✓ |
| 수강 취소 가능 기간 제한 (결제 후 N일) | ✕ |
| 대기열(Waitlist) 기능 | ✕ |
| 강의별 수강생 목록 조회 (강사 전용) | ✕ |

### 미구현 (범위 외)

- 실제 인증 시스템 (현재 `X-User-Id` 헤더로 대체)
- 외부 결제 시스템 연동

<br>

---

## AI 활용 범위

**사용 도구**: Claude

**활용 영역**:
- 설계 결정 토론
- 코드 초안 작성
- 테스트 데이터 및 HTTP 요청 파일 작성
- README 문서 구조화

<br>

---

## API 목록 및 예시

### 공통 사항

| 항목 | 값 |
| --- | --- |
| Base URL | `http://localhost:8080` |
| Content-Type | `application/json` |
| 사용자 식별 | `X-User-Id` 헤더 |

### 강의 (Course)

#### 1. 강의 등록 (강사)

```http
POST /api/courses
X-User-Id: 1
Content-Type: application/json

{
  "title": "스프링 부트 입문",
  "description": "백엔드 개발 기초",
  "price": 50000,
  "capacity": 30,
  "startDate": "2026-06-01",
  "endDate": "2026-08-31"
}
```

**응답 (201 Created)**
- Location 헤더에 생성된 강의 URI 포함

#### 2. 강의 목록 조회

```http
GET /api/courses?status=OPEN&size=20&page=0
```

- `status` (선택): `OPEN` 또는 `CLOSED` — 미지정 시 둘 다 노출 (OPEN 우선)
- `?status=DRAFT` 요청 시 400 응답

#### 3. 강의 상세 조회

```http
GET /api/courses/{courseId}
```

#### 4. 강의 모집 시작

```http
POST /api/courses/{courseId}/open
X-User-Id: 1
```

#### 5. 강의 모집 마감

```http
POST /api/courses/{courseId}/close
X-User-Id: 1
```

### 수강신청 (Enrollment)

#### 1. 수강 신청

```http
POST /api/enrollments
X-User-Id: 2
Content-Type: application/json

{
  "courseId": 1
}
```

#### 2. 결제 확정

```http
POST /api/enrollments/{enrollmentId}/confirm
X-User-Id: 2
```

#### 3. 수강 취소

```http
POST /api/enrollments/{enrollmentId}/cancel
X-User-Id: 2
```

#### 4. 내 수강신청 목록

```http
GET /api/enrollments/my?size=20&page=0
X-User-Id: 2
```

**응답 예시**

```json
{
  "content": [
    {
      "id": 1,
      "status": "CONFIRMED",
      "enrolledAt": "2026-05-23T14:00:00",
      "confirmedAt": "2026-05-23T14:05:00",
      "cancelledAt": null,
      "course": {
        "id": 1,
        "title": "스프링 부트 입문",
        "price": 50000,
        "startDate": "2026-06-01",
        "endDate": "2026-08-31"
      }
    }
  ],
  "page": 0,
  "size": 20,
  "hasNext": false
}
```

### 에러 응답 형식

```json
{
  "code": "C_008",
  "message": "정원이 초과되었습니다",
  "status": 409
}
```

<br>

---

## 데이터 모델 설명

### User

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long (PK) | |
| name | String | 사용자명 |
| email | String | 이메일 |
| role | Enum | `STUDENT`, `TEACHER` |

### Course

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long (PK) | |
| teacherId | Long | 강사 ID (User 참조) |
| title | String | 강의명 |
| description | Text | 설명 |
| price | BigDecimal | 가격 |
| capacity | int | 최대 정원 |
| enrolledCount | int | 현재 신청 인원 (PENDING + CONFIRMED) |
| startDate | LocalDate | 수강 시작일 |
| endDate | LocalDate | 수강 종료일 |
| status | Enum | `DRAFT`, `OPEN`, `CLOSED` |

### Enrollment

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long (PK) | |
| studentId | Long | 학생 ID (User 참조) |
| courseId | Long | 강의 ID (Course 참조) |
| status | Enum | `PENDING`, `CONFIRMED`, `CANCELLED` |
| enrolledAt | LocalDateTime | 신청 시각 |
| confirmedAt | LocalDateTime | 결제 확정 시각 (nullable) |
| cancelledAt | LocalDateTime | 취소 시각 (nullable) |

### BaseEntity (공통)

모든 엔티티에 `createdAt`, `updatedAt` 자동 관리 (JPA Auditing).

<img width="544" height="733" alt="스크린샷 2026-05-24 오후 11 41 26" src="https://github.com/user-attachments/assets/fbf303dc-9a5b-4090-bc83-b51449a78488" />

<br>

---

## 테스트 실행 방법

### 전체 테스트

```bash
./gradlew test
```

### 주요 테스트

| 테스트 클래스 | 검증 내용 |
| --- | --- |
| `CourseServiceTest` | 강의 등록/조회/상태 전이 |
| `EnrollmentServiceTest` | 신청/확정/취소, 권한 검증 |
| `EnrollmentConcurrencyTest` | 동시성 — 강의의 마지막 자리에 50명 동시 신청 시 정확히 1명만 성공 |
