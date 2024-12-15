package com.example.common.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // 메서드에 적용
@Retention(RetentionPolicy.RUNTIME) // 런타임 동안 유지
public @interface DistributedLock {
    String key(); // 락 키
    long leaseTime() default 5; // 락 유지 시간 (초 단위)
}
