package com.rentalplatform.backend.common.logging;

import jakarta.servlet.*;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

public class TraceIdFilter implements Filter {
    public static final String TRACE_ID = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            String traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID, traceId);

            chain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}
