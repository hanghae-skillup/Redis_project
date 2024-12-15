package com.sparta.multibookingservice.aop;

import com.sparta.dto.booking.BookingRequestDto;
import com.sparta.exception.MovieException;
import com.sparta.multibookingservice.annotation.DistributedLock;
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
public class DistributedLockAop {
    private final RedissonClient redissonClient;
    private static final String LOCK_PREFIX = "LOCK:";

    @Around("@annotation(distributedLock)")
    public Object executeLock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String key = LOCK_PREFIX + createKey(joinPoint, distributedLock);
        RLock lock = redissonClient.getLock(key);
        log.info("Try to get lock for key: {}", key);

        if (!lock.tryLock(0, distributedLock.leaseTime(), TimeUnit.SECONDS)) {
            log.info("Failed to acquire lock for key: {}", key);
            throw new MovieException("Failed to acquire lock");
        }

        log.info("Lock acquired for key: {}", key);
        try {
            return joinPoint.proceed();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("Lock released for key: {}", key);
            }
        }
    }

    private String createKey(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof BookingRequestDto request) {
            return request.screeningId().toString();
        }
        return "default";
    }
}