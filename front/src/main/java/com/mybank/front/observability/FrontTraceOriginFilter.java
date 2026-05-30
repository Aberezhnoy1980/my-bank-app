package com.mybank.front.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Front UI is the trace entry point: always start a new root span for browser requests.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnBean(Tracer.class)
public class FrontTraceOriginFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    public FrontTraceOriginFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Span span = tracer.nextSpan().name("front " + request.getMethod() + " " + request.getRequestURI()).start();
        try (Tracer.SpanInScope ignored = tracer.withSpan(span)) {
            filterChain.doFilter(request, response);
        } finally {
            span.end();
        }
    }
}
