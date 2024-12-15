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


## 4. API 테스트 파일

### **위치**
- api 모듈 내 api/src/main/resources/http 폴터듸 `api-tests.http` 파일을 참고해주세요.

### 5. 조회 성능 개선 
### **Movie 테이블 조회 성능 개선**
- **문제점**: `Movie` 테이블의 `genre` 컬럼에 대한 조회 성능이 떨어짐
- **해결 방안1**: `genre` 컬럼에 인덱스를 추가하여 조회 성능을 개선
    ```sql
    CREATE INDEX idx_movies_release_date ON movies (release_date DESC);
    CREATE INDEX idx_movies_title ON movies (title);
    CREATE INDEX idx_movies_genre ON movies (genre);  
    ```
- **해결 방안2**: 레디스 캐싱 추가 
    - `genre` 컬럼 값을 레디스에 캐싱하여 조회 성능을 개선
    - 레디스에 캐싱된 데이터를 조회하여 DB 조회 횟수를 줄임
    - 레디스에 캐싱된 데이터가 없을 경우 DB에서 조회한 후 레디스에 저장


# 부하 테스트 계획 및 결과

## 테스트 계획서

### Throughput (RPS)

#### DAU(Daily Active User) 추정
1. **1일 사용자 수(DAU)**: 5,000명
2. **1명당 1일 평균 요청 수**: 5회

#### 1일 총 접속 수 계산
1. **1일 총 접속 수** = DAU × 1명당 1일 평균 요청 수
  - 1일 총 접속 수 = 5,000 × 5 = **25,000**

#### 1일 평균 RPS 계산
1. **1일 평균 RPS** = 1일 총 접속 수 ÷ 86,400 (초/일)
  - 1일 평균 RPS = 25,000 ÷ 86,400 ≈ **0.29 RPS**

#### 1일 최대 RPS 계산
1. **최대 집중률**: 피크 시간대 트래픽이 평균의 10배로 증가한다고 가정.
2. **1일 최대 RPS** = 1일 평균 RPS × 최대 집중률
  - 1일 최대 RPS = 0.29 × 10 = **2.9 RPS**

### 테스트 목표
1. **Throughput 목표**:
  - 초당 요청 수(RPS)가 최대 2.9까지 증가하는 상황을 가정.
2. **Latency 목표**:
  - p(95) 응답 시간이 200ms를 초과하지 않아야 함.
3. **실패율**:
  - 요청 실패율이 1% 미만이어야 함.

### 테스트 시나리오
1. **평균 부하 테스트**:
  - RPS 0.29를 기준으로 부하 테스트를 수행.
  - 1초당 1명의 사용자가 요청하며, 부하를 점진적으로 증가.
2. **최대 부하 테스트**:
  - RPS 2.9를 목표로, 최대 부하 상황을 시뮬레이션.

### 테스트 설정
1. **테스트 데이터**:
  - 영화 데이터 1,000건을 사용.
2. **테스트 시나리오**:
  - 점진적 부하 증가와 최대 부하를 유지하는 단계를 포함.

---

## 테스트 시나리오 (test.js)

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '10s', target: 1 },   // 10초 동안 평균 부하(0.29 RPS)
        { duration: '10s', target: 3 },   // 10초 동안 최대 부하(2.9 RPS)
        { duration: '10s', target: 0 },   // 10초 동안 부하 감소
    ],
};

export default function () {
    // 테스트할 요청
    const url = 'http://localhost:8080/movies/search?title=Movie&genre=DRAMA'; // title, genre 필터링

    // HTTP GET 요청
    const res = http.get(url);

    // 응답 검증
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 200ms': (r) => r.timings.duration < 200, // 응답 시간 검증
    });

    sleep(1); // 요청 간 1초 대기
}
```

---


### Redis 캐시 키 생성 로직
```java
@Bean
public KeyGenerator customKeyGenerator() {
    return (target, method, params) -> {
        String key = method.getName() + "_" + String.join("_", Arrays.stream(params)
            .map(String::valueOf)
            .toArray(String[]::new));
        return key;
    };
}
```

### Redis 캐시 만료 설정
```java

@Bean
public RedisCacheConfiguration cacheConfiguration() {
    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10)) // 캐시 만료 시간: 10분
        .disableCachingNullValues();      // null 값 캐싱 방지
}
```

---

## 부하 테스트 결과 요약

| 단계                | 평균 응답 시간(ms) | 최대 응답 시간(ms) | 요청 성공률(%) | 초당 처리 요청(req/s) |
|---------------------|-------------------|-------------------|----------------|-----------------------|
| **기본 상태**       | 56.33            | 463.00            | 94.18          | 1.41                  |
| **인덱스 적용**     | 14.58            | 151.33            | 100.00         | 1.50                  |
| **인덱스+캐싱 적용** | 6.64             | 17.42             | 100.00         | 1.52                  |

### 결과 분석
1. **기본 상태**:
  - 평균 응답 시간과 최대 응답 시간이 높으며, 실패율이 발생.
2. **인덱스 적용**:
  - 인덱스를 추가하며 응답 시간이 대폭 개선되고 실패율 제거.
3. **인덱스+캐싱 적용**:
  - 캐싱으로 인해 평균 응답 시간이 가장 짧아짐. 전체적인 성능이 안정적.

### 최종 결론
- 인덱스와 캐싱은 성능 개선에 효과적이며, 특히 캐싱은 조회 성능을 극대화.
- Redis 캐시 미적중의 영향을 최소화하기 위해 캐싱 정책 최적화 필요.

