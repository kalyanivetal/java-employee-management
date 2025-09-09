package com.reliaquest.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.reliaquest.api.web.RateLimitingInterceptor;
import com.reliaquest.api.web.SimpleRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitingInterceptorTest {

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

        // Mock response writer so we can verify output written on rate limit exceeded
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
    void testBlockRequestWhenNoPermit() throws Exception {
        when(rateLimiter.tryAcquire()).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(429);
        assertTrue(responseWriter.toString().contains("Too many requests"));
    }
}
