package com.reliaquest.api.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitingInterceptorTest {

    private SimpleRateLimiter mockLimiter;
    private RateLimitingInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        mockLimiter = mock(SimpleRateLimiter.class);
        interceptor = new RateLimitingInterceptor(mockLimiter);

        try {
            var field = RateLimitingInterceptor.class.getDeclaredField("isRateLimiterEnabled");
            field.setAccessible(true);
            field.set(interceptor, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        handler = new Object();
    }

    @Test
    void testPreHandle_RateLimiterDisabled() throws Exception {
        // Simulate rateLimiter.enabled=false
        var field = RateLimitingInterceptor.class.getDeclaredField("isRateLimiterEnabled");
        field.setAccessible(true);
        field.set(interceptor, false);

        boolean result = interceptor.preHandle(request, response, handler);
        assertTrue(result);
        verify(mockLimiter, never()).tryAcquire();
    }

    @Test
    void testPreHandle_RateLimitExceeded() throws Exception {
        when(mockLimiter.tryAcquire()).thenReturn(false);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(printWriter);

        boolean result = interceptor.preHandle(request, response, handler);

        assertFalse(result);
        verify(response).setStatus(429);
        printWriter.flush();
        assertEquals("Too many requests - rate limit exceeded", responseWriter.toString());
    }

    @Test
    void testPreHandle_RateLimitAllowed() throws Exception {
        when(mockLimiter.tryAcquire()).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, handler);

        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }
}
