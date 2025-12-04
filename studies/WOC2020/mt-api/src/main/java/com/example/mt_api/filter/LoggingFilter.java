package com.example.mt_api.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// @Component
// @Order(-101) // Ensures this runs before security filters
public class LoggingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class); // Logger instance for logging.

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Log request details before processing the request.
        logger.info("Request: Method={}, URI={}, Headers={}\n",
                request.getMethod(),
                request.getRequestURI(),
                getRequestHeaders(request));

        long startTime = System.currentTimeMillis(); // Capture the start time to measure processing duration.

        try {
            filterChain.doFilter(request, response); // Continue with the next filter in the chain.
        } finally {
            long duration = System.currentTimeMillis() - startTime; // Calculate how long the request took.

            // Log response details after request processing.
            logger.info("Response: Status={}, URI={}, Duration={}ms, Headers={}\n",
                    response.getStatus(),
                    request.getRequestURI(),
                    duration,
                    getResponseHeaders(response));

            // Log any exceptions that occurred during processing.
            if (request.getAttribute("javax.servlet.error.exception") != null) {
                logger.error("Exception during request processing",
                        (Exception) request.getAttribute("javax.servlet.error.exception"));
            }
        }
    }

    // Utility method to extract request headers for logging.
    private String getRequestHeaders(HttpServletRequest request) {
        return enumerationAsStream(request.getHeaderNames())
                .map(header -> String.format("\n\t{%s}={%s}", header, request.getHeader(header)))
                .collect(Collectors.joining(""));
    }

    // Utility method to extract response headers for logging.
    private String getResponseHeaders(HttpServletResponse response) {
        return response.getHeaderNames().stream()
                .map(header -> String.format("\n\t{%s}={%s}", header, response.getHeader(header)))
                .collect(Collectors.joining(""));
    }

    private <T> Stream<T> enumerationAsStream(Enumeration<T> enumeration) {
        return StreamSupport.stream(
                ((Iterable<T>) () -> new Iterator<T>() {
                    @Override
                    public boolean hasNext() {
                        return enumeration.hasMoreElements();
                    }

                    @Override
                    public T next() {
                        return enumeration.nextElement();
                    }
                }).spliterator(),
                false // false for sequential stream
        );
    }
}
