package com.reliaquest.api.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimiterTest {

    private SimpleRateLimiter rateLimiter;
    private RateLimitingInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        rateLimiter = mock(SimpleRateLimiter.class);
        interceptor = new RateLimitingInterceptor(rateLimiter);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testAllowRequestWhenPermitAvailable() throws Exception {
        when(rateLimiter.tryAcquire()).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
        assertEquals("", responseWriter.toString());
    }

    @Test
    void testBlockRequestWhenNoPermit1() throws Exception {
        SimpleRateLimiter rateLimiter = mock(SimpleRateLimiter.class);
        RateLimitingInterceptor interceptor = new RateLimitingInterceptor(rateLimiter);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        StringWriter responseWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(responseWriter);

        when(rateLimiter.tryAcquire()).thenReturn(false);
        when(response.getWriter()).thenReturn(printWriter);

        Field field = RateLimitingInterceptor.class.getDeclaredField("isRateLimiterEnabled");
        field.setAccessible(true);
        field.set(interceptor, true);

        boolean result = interceptor.preHandle(request, response, new Object());

        printWriter.flush();

        assertFalse(result);
        verify(response).setStatus(429);
        assertTrue(responseWriter.toString().contains("Too many requests"));
    }
}
