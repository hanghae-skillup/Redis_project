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