package com.reliaquest.api.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimpleRateLimiterTest {

    @Test
    void testTryAcquire_AllowsPermit() {
        SimpleRateLimiter limiter = new SimpleRateLimiter(100);
        boolean result = limiter.tryAcquire();
        assertTrue(result);
    }

    @Test
    void testTryAcquire_DeniesPermitWhenRateIsLow() throws InterruptedException {
        SimpleRateLimiter limiter = new SimpleRateLimiter(0.0001);

        limiter.tryAcquire();
        Thread.sleep(10);
        boolean result = limiter.tryAcquire();
        assertFalse(result);
    }
}
