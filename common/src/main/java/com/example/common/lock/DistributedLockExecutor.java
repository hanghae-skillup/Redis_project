package com.example.common.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class DistributedLockExecutor {

    private final RedissonClient redissonClient;

    public DistributedLockExecutor(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 분산 락을 사용하여 지정된 작업을 실행
     *
     * @param lockKey     Redis 락 키
     * @param leaseTime   락 유지 시간 (초)
     * @param task        실행할 작업
     * @param <T>         작업 반환 타입
     * @return 작업 결과
     */
    public <T> T executeWithLock(String lockKey, long leaseTime, Supplier<T> task) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            // 락 획득 시도
            isLocked = lock.tryLock(0, leaseTime, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new IllegalStateException("현재 다른 프로세스에서 작업 중입니다. 다시 시도해주세요.");
            }
            // 작업 실행
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트 발생.", e);
        } finally {
            // 락 해제
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
