package com.incidentcomm.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        log.info("Request: {} {}", method, path);
        
        // Log relevant headers for debugging purposes
        List<String> relevantHeaders = List.of("Authorization", "Content-Type", "User-Agent");
        relevantHeaders.forEach(header -> {
            if (request.getHeaders().containsKey(header)) {
                if (header.equals("Authorization")) {
                    log.debug("Header: {}: {}", header, "Bearer ***");
                } else {
                    log.debug("Header: {}: {}", header, request.getHeaders().getFirst(header));
                }
            }
        });
        
        // Record the start time
        long startTime = System.currentTimeMillis();
        
        // Continue the filter chain
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // Calculate the time taken
            long duration = System.currentTimeMillis() - startTime;
            log.info("Response: {} {} - Status: {} - Duration: {}ms", 
                    method, path, exchange.getResponse().getStatusCode(), duration);
        }));
    }

    @Override
    public int getOrder() {
        return 0; // After JwtAuthenticationFilter but before other filters
    }
} 