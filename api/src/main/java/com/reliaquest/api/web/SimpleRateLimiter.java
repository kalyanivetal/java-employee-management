package com.reliaquest.api.web;

import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SimpleRateLimiter {
    private final RateLimiter rateLimiter;

    public SimpleRateLimiter(@Value("${rateLimiter.permitsPerSecond:2}") double permitsPerSecond) {
        this.rateLimiter = RateLimiter.create(permitsPerSecond);
    }

    public boolean tryAcquire() {
        return rateLimiter.tryAcquire();
    }
}
