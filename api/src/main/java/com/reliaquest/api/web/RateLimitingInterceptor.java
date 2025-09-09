package com.reliaquest.api.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    @Value("${rateLimiter.enabled:true}")
    private boolean isRateLimiterEnabled;

    private final SimpleRateLimiter simpleRateLimiter;

    public RateLimitingInterceptor(SimpleRateLimiter simpleRateLimiter) {
        this.simpleRateLimiter = simpleRateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!isRateLimiterEnabled) {
            return true;
        }
        if (!simpleRateLimiter.tryAcquire()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests - rate limit exceeded");
            return false;
        }
        return true;
    }
}
