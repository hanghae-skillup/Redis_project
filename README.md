# Redis_project

## Multi Module

### API Gateway
- 클라이언트에서의 요청을 검증하고 각 서비스에 맞게 라우팅합니다. (해당 프로젝트에서는 검증 로직 생략)

### movie-service
- 현재 상영중인 영화를 보여주는 **메인 서비스**입니다.
- 해당 모듈에 DB가 연결되어 있고, DB의 ORM은 해당 서비스에서 관리합니다.
- 1주차 구현의 API의 컨트롤러가 해당 모듈에 구현되어 있습니다.

### cinema-service
- 상영관을 관리하는 서비스입니다.
- 상영관을 등록하고 조회하며, 좌석(Seat) 관리의 역할을 가지고 있습니다.

### booking-service
- 영화 예약을 담당하는 서비스입니다.
- 현재 상영(Screening)에 대한 좌석 예매하며, 중복 예매 방지, 예매 정보(고객 이름, 전화번호)를 저장합니다.
---
## Table Design
- Movies (영화 정보 저장)
  - rating: 영화 관람등급 (ALL, TWELVE, FIFTEEN, ADULT)
  - genres: 영화 장르
  - duration: 상영시간(단위: 분)
- Theaters
  - 상영관 정보 
- Seats 
  - 좌석 정보 저장 
  - 5x5 형태의 좌석 구조 (A1-E5)
  - theater_id로 어떤 상영관의 좌석인지 구분 
- Screenings

  - 영화 상영 일정 (현재 예약 가능한 영화)
  - movie_id와 theater_id로 어떤 영화가 어느 상영관에서 상영되는지 구분

- Reservations
  - 예매 정보를 저장하는 테이블
  - screening_id로 어떤 상영에 대한 예매인지 구분
  - seat_id로 어떤 좌석이 예매되었는지 구분
  - customer_name과 phone_number로 예매자 정보 관리

- Notes
  - 모든 테이블은 생성일자(created_at)와 수정일자(updated_at) 포함
  - 외래키 제약조건을 통해 데이터 정합성 보장
  - 인덱스는 기본키(PK)만 사용