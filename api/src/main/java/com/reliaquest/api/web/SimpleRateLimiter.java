package com.reliaquest.api.web;

import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimpleRateLimiter {
    private static final Logger log = LoggerFactory.getLogger(SimpleRateLimiter.class);

    private final RateLimiter rateLimiter;

    @Value("${rateLimiter.retry.maxAttempts:3}")
    private int maxRetryAttempts;

    @Value("${rateLimiter.retry.initialBackoffMillis:100}")
    private long initialBackoffMillis;

    @Value("${rateLimiter.retry.backoffMultiplier:2.0}")
    private double backoffMultiplier;

    @Value("${rateLimiter.retry.maxBackoffMillis:1000}")
    private long maxBackoffMillis;

    public SimpleRateLimiter(@Value("${rateLimiter.permitsPerSecond:1}") double permitsPerSecond) {
        this.rateLimiter = RateLimiter.create(permitsPerSecond);
    }

    public boolean tryAcquireWithRetry() {
        int attempt = 0;
        long backoff = initialBackoffMillis;

        while (attempt < maxRetryAttempts) {
            if (rateLimiter.tryAcquire()) {
                if (attempt > 0) {
                    log.info("Acquired permit after {} retry attempt(s)", attempt);
                }
                return true;
            }

            attempt++;
            try {
                log.warn("Rate limit exceeded, retry attempt {} after backoff {} ms", attempt, backoff);
                Thread.sleep(backoff);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted during backoff sleep", e);
                return false;
            }
            backoff = Math.min(maxBackoffMillis, (long) (backoff * backoffMultiplier));
        }
        log.error("Failed to acquire permit after {} attempts", maxRetryAttempts);
        return false;
    }

    public boolean tryAcquire() {
        return rateLimiter.tryAcquire();
    }
}
