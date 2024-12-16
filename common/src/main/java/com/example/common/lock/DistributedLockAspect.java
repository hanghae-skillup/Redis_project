package com.example.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(distributedLock)")
    public Object handleDistributedLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = distributedLock.key();
        long leaseTime = distributedLock.leaseTime();

        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            isLocked = lock.tryLock(0, leaseTime, TimeUnit.SECONDS); // 락 시도
            if (!isLocked) {
                log.warn("Unable to acquire lock for key: {}", lockKey);
                throw new IllegalStateException("현재 작업이 다른 프로세스에서 실행 중입니다. 다시 시도해주세요.");
            }

            log.info("Lock acquired for key: {}", lockKey);
            return joinPoint.proceed(); // 메서드 실행
        } finally {
            if (isLocked) {
                lock.unlock(); // 락 해제
                log.info("Lock released for key: {}", lockKey);
            }
        }
    }
}