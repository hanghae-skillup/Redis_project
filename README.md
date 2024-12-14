# README

## 1. 현재 모듈 구조 및 레이어드 아키텍처

### **모듈 구조**

- **`api`**: 애플리케이션의 엔드포인트를 담당하며, 클라이언트와 직접적으로 통신합니다.
    - 주요 역할: 컨트롤러, 요청/응답 데이터의 DTO 처리
    - 예: `MovieController`

- **`common`**: 공통적으로 사용되는 유틸리티, 상수, 공통 DTO, 공통 Enum 등을 포함합니다.
    - 주요 역할: 각 모듈 간 재사용 가능한 코드 제공
    - 예: `Genre` Enum, `Response` DTO

- **`infrastructure`**: 데이터베이스와 같은 외부 시스템과의 상호작용을 담당합니다.
    - 주요 역할: Spring Data JPA 리포지토리 구현체 및 외부 시스템과의 연결
    - 예: `JpaMovieRepository`

- **`domain`**: 핵심 비즈니스 로직과 엔티티를 포함합니다.
    - 주요 역할: 비즈니스 규칙을 정의하고 객체 간 관계를 관리
    - 예: `Movie`, `Seat`, `Reservation`

- **`service`**: 비즈니스 로직을 처리하며, API와 도메인 간의 다리 역할을 합니다.
    - 주요 역할: 트랜잭션 관리 및 비즈니스 규칙 구현
    - 예: `MovieService`

### **레이어드 아키텍처**

#### **Presentation Layer**
- 위치: `api` 모듈
- 역할: 클라이언트와의 상호작용 처리 (HTTP 요청/응답)

#### **Application Layer**
- 위치: `service` 모듈
- 역할: 비즈니스 로직과 도메인 객체 호출

#### **Domain Layer**
- 위치: `domain` 모듈
- 역할: 핵심 비즈니스 로직 및 규칙 구현

#### **Infrastructure Layer**
- 위치: `infrastructure` 모듈
- 역할: 데이터베이스와의 통신 및 외부 시스템과의 상호작용

---

## 2. 테이블에 대한 설명

### **Movie 테이블**
- **역할**: 영화 정보를 저장하는 테이블
- **컬럼**:
    - `id` (PK): 영화의 고유 식별자
    - `title`: 영화 제목
    - `genre`: 영화 장르 (예: 액션, 드라마)
    - `release_date`: 영화의 개봉일
    - `running_minutes`: 상영 시간 (분 단위)
    - `thumbnail_url`: 썸네일 이미지 URL
    - `showing`: 현재 상영 여부
    - `ageRating`: 관람 등급 (예: 12세 이상, 15세 이상)
  
### **Seat 테이블**
- **역할**: 영화관 좌석 정보를 저장하는 테이블
- **컬럼**:
    - `id` (PK): 좌석 고유 식별자
    - `row`: 좌석의 행 (예: A, B, C)
    - `number`: 좌석의 번호 (예: 1, 2, 3)
    - `is_reserved`: 예약 여부

### **Reservation 테이블**
- **역할**: 좌석 예약 정보를 저장하는 테이블
- **컬럼**:
    - `id` (PK): 예약의 고유 식별자
    - `user_id` (FK): 예약한 사용자 ID
    - `movie_id` (FK): 예약한 영화 ID
    - `seat_id` (FK): 예약된 좌석 ID
    - `reserved_date`: 예약이 이루어진 날짜

### **User 테이블**
- **역할**: 사용자 정보를 저장하는 테이블
- **컬럼**:
    - `id` (PK): 사용자 고유 식별자
    - `name`: 사용자 이름
    - `email`: 사용자 이메일
    - `password`: 암호화된 비밀번호

---


### **Timestamped 추상클래스**
- **역할**: 생성, 수정 시간을 관리하는 추상 클래스로 모든 엔티티가 상속받음
- **컬럼**:
    - `created_at`: 생성 시간
    - `updated_at`: 수정 시간
    - `created_by`: 생성자
    - `updated_by`: 수정자
---

## 3. 실행 방법

### **Docker Compose로 실행**
1. `docker-compose.yml` 파일을 작성하고 MySQL과 애플리케이션을 구성합니다.
   ```yaml
   version: '3.8'
   services:
     db:
       image: mysql:8.1
       container_name: project_mysql
       environment:
         MYSQL_ROOT_PASSWORD: root_password
         MYSQL_DATABASE: project_db
         MYSQL_USER: user
         MYSQL_PASSWORD: user_password
       ports:
         - "3307:3306"
       volumes:
         - db_data:/var/lib/mysql

   volumes:
     db_data:
   ```

2. `docker-compose up` 명령어를 실행합니다:
   ```bash
   docker-compose up --build
   ```

### **로컬 실행**
1. Gradle로 프로젝트 빌드:
   ```bash
   ./gradlew clean build
   ```
2. API 모듈 실행:
   ```bash
   ./gradlew :api:bootRun
   ```

---

이 프로젝트는 멀티 모듈 구조와 레이어드 아키텍처를 활용하여 확장성과 유지보수성을 극대화한 설계를 목표로 합니다.

