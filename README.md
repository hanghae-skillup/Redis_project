# Redis_project

## Multi Module Role
### multi-movie-service (PORT: 8081)
- 현재 상영중인 영화를 보여주는 **메인 서비스**입니다.
- 해당 모듈에 DB가 연결되어 있고, DB의 ORM은 해당 서비스에서 관리합니다.
- 1,2주차 구현의 API 컨트롤러가 해당 모듈에 구현되어 있습니다.
### multi-theaterEntity-service (PORT: 8082)
- 상영관을 관리하는 서비스입니다.
- 상영관을 등록하고 조회하며, 좌석(Seat) 관리의 역할을 가지고 있습니다.
### multi-booking-service (PORT: 8083)
- 영화 예약을 담당하는 서비스입니다.
- 현재 상영(Screening)에 대한 좌석 예매하며, 중복 예매 방지, 예매 정보(고객 이름, 전화번호)를 저장합니다.

## Table Design
- Movies (영화 정보 저장)
    - rating: 영화 관람등급 (ALL, TWELVE, FIFTEEN, ADULT)
    - genres: 영화 장르
    - duration: 상영시간(단위: 분)
- Theaters
    - 상영관 정보
- Seats
    - 상영관 좌석 정보
    - 5x5 형태의 좌석 구조 (A1-E5)
    - theater_id로 어떤 상영관의 좌석인지 구분
- Screenings
    - 영화 상영 일정 (현재 예약 가능한 영화)
    - movie_id와 theater_id로 어떤 영화가 어느 상영관에서 상영되는지 구분
- Booking
    - 예매 정보
    - screening_id로 어떤 상영에 대한 예매인지 구분
    - seat_id로 어떤 좌석이 예매되었는지 구분
    - usre_id와 phone_number로 예매자 정보 관리
- Notes
    - 모든 테이블은 생성일자(created_at)와 수정일자(updated_at) 포함
    - 외래키 제약조건을 통해 데이터 정합성 보장
    - 인덱스는 기본키(PK)만 사용

# Test summary
## 1. 캐싱 데이터 설계
### 1.1 캐싱 대상 데이터
- 메인 페이지의 전체 영화 목록
    - 조회가 자주 발생하며, 데이터 변경이 자주 일어나지 않기 때문
- 장르별 필터링된 영화 목록
  - 사용자들이 특정 장르를 자주 검색할 것으로 예상
### 1.2 캐시 정책
- Cache-Aside 패턴 적용
- TTL(Time To Live): 1시간
- 캐시 키 패턴:
  - 기본 프리픽스: ```movies::search::```
  - 전체 목록: ```movies::search::all```
  - 제목 검색: ```movies::search::title::{searchTerm}```
  - 장르 검색: ```movies::search::genres::{genreName}```
  - 제목+장르 검색: ```movies::search::title::{searchTerm}::genres::{genreName}```

## 2. 테스트 환경 설정
### 2.1 테스트 데이터
- 총 영화 데이터: 500개
- 장르 분포: 10개 장르, 각 50개 영화
- 데이터 구조:
```
Movie {
  title: String (평균 30자)
  rating: String (5자)
  releaseDate: Date
  thumbnailUrl: String (100자)
  duration: Integer
  genre: String (10자)
}
```
- 예상 데이터 크기: ~100KB/건 × 500건 = ```~50MB```

### 2.2 성능 목표

1. Throughput
   - 목표 RPS: 5,000 (피크 시간)
   - 평균 RPS: 500

2. Latency
   - p95 < 3,000ms (3초)
   - p99 < 5,000ms (5초)
   - 평균 응답시간 < 2,000ms (2초)

3. Error Rate
   - 전체 오류율 < 1%
   - 시스템 오류 (5xx) < 0.1%

4. Sort Validation
   - 정렬 성공률 > 95%

5. Memory Usage
   - 최대 메모리: 2GB 미만
   - 평균 메모리: 1GB 미만
   - RSS 메모리(p95): 1.5GB 미만

6. 부하 조건
   - 지속 시간: 10분
   - 단계별 부하:
     - 램프 업: 2분
     - 피크 부하: 5분
     - 램프 다운: 3분

## 3. 단계별 테스트 계획
### 3.1 검색 기능 추가 후 (Indexing 적용 전)
```sql
SELECT m.title, m.rating, m.release_date, m.thumbnail_url, m.duration, m.genre
FROM movies m
WHERE m.title LIKE :searchTerm
ORDER BY m.release_date DESC
```

### 3.2 Indexing 적용 
```mysql
CREATE INDEX idx_movies_title ON movies (title);
CREATE INDEX idx_movies_release_date ON movies (release_date);
CREATE INDEX idx_movies_genre ON movies (genre);
```

### 3.3 Caching 적용
```JAVA
@Cacheable(value = "movieSearch", key = "#searchTerm")
public List<MovieProjection> searchMovies(String searchTerm) {
    // 로직
}
```

## 4. 테스트 시나리오
### 4.1 K6 테스트 스크립트
<details>
<summary>테스트 스크립트 코드</summary>

```Javascript
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

const errorRate = new Rate('error_rate');
const cacheHitRate = new Rate('cache_hit_rate');
const requestDuration = new Trend('request_duration');
const sortValidationRate = new Rate('sort_validation_rate');

const memoryUsed = new Trend('memory_used');
const memoryTotal = new Trend('memory_total');
const memoryRss = new Trend('memory_rss');

export const options = {
    stages: [
      { duration: '2m', target: 1000 },  // 램프 업
      { duration: '5m', target: 5000 },  // 피크 부하
      { duration: '3m', target: 0 }      // 램프 다운
    ],
    thresholds: {
      http_req_duration: ['p(95)<3000'],    // 3초
      error_rate: ['rate<0.01'],            // 1% 미만
      sort_validation_rate: ['rate>0.95'],  // 95% 이상
      memory_used: ['avg<1000'],            
      memory_total: ['max<2000'],           
      memory_rss: ['p(95)<1500']            // RSS 메모리 95p 1.5GB 미만
    },
  };

const MOVIE_TITLES = [
    "Great", "Secret", "Last", "New", "Mysterious",
    "Golden", "Shining", "Eternal", "Sweet", "Cold"
];

const GENRES = [
    "Action", "Drama", "Comedy", "Romance", "Horror",
    "SF", "Animation", "Documentary", "Thriller", "Fantasy"
];

function isReleaseDateSorted(movies) {
  try {
    if (!Array.isArray(movies)) {
      console.log('Response is not an array:', movies);
      return false;
    }
    
    for (let i = 1; i < movies.length; i++) {
      const prevDate = new Date(movies[i-1].releaseDate);
      const currDate = new Date(movies[i].releaseDate);
      if (isNaN(prevDate.getTime()) || isNaN(currDate.getTime())) {
        console.log('Invalid date found:', movies[i-1].releaseDate, movies[i].releaseDate);
        return false;
      }
      if (prevDate < currDate) {
        return false;
      }
    }
    return true;
  } catch (e) {
    console.error('Error in sort validation:', e);
    return false;
  }
}

function buildQueryString(params) {
  const queryParts = [];
  for (const [key, value] of Object.entries(params)) {
    if (value !== null && value !== undefined) {
      queryParts.push(`${key}=${encodeURIComponent(value)}`);
    }
  }
  return queryParts.length > 0 ? '?' + queryParts.join('&') : '';
}

function recordMetrics(response, checkName) {
  let movies;
  try {
    movies = JSON.parse(response.body);
  } catch (e) {
    console.error('Failed to parse response body:', response.body);
    return false;
  }

  const isSorted = isReleaseDateSorted(movies);

  const checks = check(response, {
    [`${checkName} status is 200`]: (r) => r.status === 200,
    [`${checkName} response time OK`]: (r) => r.timings.duration < 5000,
    [`${checkName} release date is sorted`]: () => isSorted
  });

  requestDuration.add(response.timings.duration);
  errorRate.add(response.status !== 200);
  sortValidationRate.add(isSorted);
  
  if (response.headers['X-Memory-Used']) {
    memoryUsed.add(Number(response.headers['X-Memory-Used']));
  }
  if (response.headers['X-Memory-Total']) {
    memoryTotal.add(Number(response.headers['X-Memory-Total']));
  }
  if (response.headers['X-Memory-Rss']) {
    memoryRss.add(Number(response.headers['X-Memory-Rss']));
  }

  return checks;
}

export default function () {
  const BASE_URL = 'http://localhost:8081/movies';
  const scenario = Math.random();
  
  let response;
  try {
    if (scenario < 0.5) {
      response = http.get(BASE_URL);
      recordMetrics(response, 'getCurrentMovies');
    } else {
      let queryParams = {};
      
      if (Math.random() < 0.5) {
        queryParams.title = getRandomElement(MOVIE_TITLES);
      }
      if (Math.random() < 0.5) {
        queryParams.genres = getRandomElement(GENRES);
      }
      
      if (Object.keys(queryParams).length === 0) {
        queryParams.title = getRandomElement(MOVIE_TITLES);
      }
      
      const queryString = buildQueryString(queryParams);
      response = http.get(`${BASE_URL}/search${queryString}`);
      recordMetrics(response, 'searchMovies');
    }
  } catch (e) {
    console.error('Request failed:', e);
  }

  sleep(1);
}

function getRandomElement(array) {
  return array[Math.floor(Math.random() * array.length)];
}
```
</details>

### 4.2 모니터링 지표

- 응답 시간 (Response Time)
  - 평균, p95, p99
- 처리량 (Throughput)
  - 초당 요청 수 (RPS)
- 오류율 (Error Rate)
  - 5xx, 4xx 응답 비율
- 리소스 사용률
  - CPU 사용률
  - 메모리 사용률
  - DB 연결 수

## 5. 결과 수집 및 분석 방법
### 5.1 수집할 메트릭
- JVM 메트릭
  - GC 시간
  - 힙 메모리 사용량
- 데이터베이스 메트릭
  - Query 실행 시간
  - Connection Pool 상태
- Redis 메트릭
  - Hit/Miss 비율
  - 메모리 사용량

### 5.2 분석 기준
1. 응답 시간 분포 
2. 초당 처리 요청 수 
3. 캐시 히트율 
4. DB Connection 사용률 
5. 시스템 리소스 사용률

## 6. 테스트 결과

### 테스트 환경
|구분|테스트 1 (인덱싱 적용 전)|테스트 2 (인덱싱 적용 후)|테스트 3 (인덱싱+캐싱 적용)|
|---|---|---|---|
|테스트 단계|인덱싱 적용 전|인덱싱 적용 후, 캐싱 적용 전|인덱싱과 캐싱 적용 후|
|실행 일시|2024-12-15 23:30|2024-12-15 01:20|2024-12-15 02:00|
|테스트 기간|10분|10분|10분|

### 성능 지표
|지표|테스트 1 (인덱싱 적용 전)|테스트 2 (인덱싱 적용 후)|테스트 3 (인덱싱+캐싱 적용)|
|---|---|---|---|
|평균 응답시간|6.13초|10.99초|6.32초|
|p95 응답시간|14.62초|26.29초|15.52초|
|p90 응답시간|13.27초|24.00초|14.37초|
|최대 응답시간|26.73초|43.46초|25.96초|
|최대 RPS|333.12|200.76|322.81|
|오류율|0%|0%|0%|

### 리소스 사용률
|지표|테스트 1 (인덱싱 적용 전)|테스트 2 (인덱싱 적용 후)|테스트 3 (인덱싱+캐싱 적용)|
|---|---|---|---|
|수신된 데이터|12 GB (19 MB/s)|6.9 GB (12 MB/s)|11 GB (19 MB/s)|
|전송된 데이터|22 MB (37 kB/s)|13 MB (22 kB/s)|21 MB (36 kB/s)|
|연결 설정 시간|174.69µs|706.01µs|687.98µs|
|대기 시간|6.13초|10.99초|6.31초|
|수신 시간|1.57ms|3.9ms|4.14ms|
|전송 시간|357.65µs|1.22ms|1.36ms|
|총 메모리|679.70 MB|698.04 MB|864.02 MB|
|사용된 메모리|315.13 MB|316.57 MB|377.88 MB|

### 성능 체크 결과
|지표|테스트 1 (인덱싱 적용 전)|테스트 2 (인덱싱 적용 후)|테스트 3 (인덱싱+캐싱 적용)|
|---|---|---|---|
|전체 체크 성공률|83.25% (499,965/600,504)|77.91% (282,036/361,992)|82.67% (480,976/581,796)|
|API 응답 성공률|100%|100%|100%|
|응답시간 < 3초 (searchMovies)|49%|33%|47%|
|응답시간 < 3초 (getCurrentMovies)|50%|33%|48%|
|정렬 검증 성공률|100%|100%|100%|




### 테스트1 (인덱싱 사용 전) 분석
- Throughput
  - 목표 RPS(5,000)의 6.6%인 333.12 RPS만 처리 가능
  - 높은 부하 상황에서 서버가 요청을 효과적으로 처리하지 못함
- Latency
  - 평균 응답 시간이 6.13초로 매우 느림
  - p95가 14.62초로 목표(200ms)보다 73배 이상 느림
  - p90이 13.27초로 매우 높음
  - 최대 응답 시간이 26.73초로 매우 높음
- Sort
  - 전체 정렬 검증 성공률 100% (정렬 문제 없음)
- 리소스 사용
  - 데이터 수신량이 12GB로 매우 높음
  - 전송된 데이터 22MB로 과도한 데이터 전송 발생
  - HTTP 요청 대기 시간이 평균 6.13초로 비효율적인 DB 쿼리 처리 가능성
  - 메모리 사용량: 총 679.70MB 중 315.13MB 사용 (정상 범위)
  - HTTP 연결 설정 시간 평균 174.69µs로 양호
---


### 테스트 2 (인덱싱 사용 후, 캐싱 사용 전) 분석

1. Throughput
   - 목표 RPS(5,000)의 4%인 200.76 RPS 처리 가능
   - 높은 부하 상황에서 서버가 요청을 효과적으로 처리하지 못함

2. Latency 
   - 평균 응답 시간이 10.99초로 느림
   - p95가 26.29초로 목표(200ms)보다 131배 이상 느림
   - p90이 24.00초로 매우 높음
   - 최대 응답 시간이 43.46초로 극단적으로 높음

3. sorted
   - 정렬 검증 성공률 100% (정렬 문제 없음)

4. 리소스 사용 
   - 데이터 수신량은 6.9GB로 6-1보다는 낮아졌지만 여전히 높음
   - 전송된 데이터 13MB로 6-1에 비해 40.9% 감소
   - HTTP 요청 대기 시간이 평균 10.99초로 비효율적인 DB 쿼리 처리 가능성
   - 메모리 사용량: 총 698.04MB 중 316.57MB 사용 (정상 범위)
   - HTTP 연결 설정 시간 평균 706.01µs로 양호

---


### 테스트 3 (인덱싱 사용 후, 캐싱 사용 후) 분석

1. Throughput 개선
   - RPS가 200.76에서 322.81로 60.8% 증가
   - 목표 RPS(5,000) 대비 6.4%로 소폭 상승했으나 여전히 낮음
   - 동시 처리 능력이 개선되었으나 추가 최적화 필요

2. Latency 개선
   - 평균 응답 시간 42.5% 감소 (10.99초 → 6.32초)
   - p95 41% 개선 (26.29초 → 15.52초)
   - p90 40.1% 개선 (24.00초 → 14.37초)
   - 최대 응답 시간 40.3% 감소 (43.46초 → 25.96초)

3. 정확성 및 안정성
   - 정렬 검증 성공률 100% 유지
   - 전체 체크 성공률 4.76% 증가 (77.91% → 82.67%)
   - 3초 이내 응답 비율 14% 향상 (33% → 47%)

4. 리소스 활용도
   - 데이터 처리량 59.4% 증가 (6.9GB → 11GB)
   - 대기 시간 42.6% 감소 (10.99초 → 6.31초)
   - 메모리 사용량 19.4% 증가 (총 메모리 698.04MB → 864.02MB)
   - 연결 설정 시간 2.6% 개선 (706.01µs → 687.98µs)


---

## 분산락 (AOP) lease time, wait time 적용

- waitTime = 0L
  - 0으로 설정하여 락을 즉시 획득하지 못하면 실패하도록 함
  - 동시 요청 시 한 요청만 성공하고 나머지는 즉시 실패하게 됨
- leaseTime = 3L
  - 3초로 설정하여 예약 로직이 완료될 때까지 충분한 시간 보장

