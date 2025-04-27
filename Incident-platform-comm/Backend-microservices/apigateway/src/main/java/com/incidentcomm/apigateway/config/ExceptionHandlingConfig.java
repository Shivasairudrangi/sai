package com.incidentcomm.apigateway.config;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Configuration
public class ExceptionHandlingConfig {

    @Bean
    @Order(-1)
    public ErrorWebExceptionHandler exceptionHandler() {
        return (ServerWebExchange exchange, Throwable ex) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            String message = "Internal Server Error";

            if (ex instanceof ResponseStatusException) {
                HttpStatusCode statusCode = ((ResponseStatusException) ex).getStatusCode();
                message = ex.getMessage();
                if (statusCode instanceof HttpStatus) {
                    status = (HttpStatus) statusCode;
                } else {
                    // If it's not an HttpStatus instance, use the raw status code
                    status = HttpStatus.valueOf(statusCode.value());
                }
            }

            response.setStatusCode(status);

            String errorJson = String.format("{\"error\":\"%s\",\"message\":\"%s\",\"status\":%d}",
                    status.getReasonPhrase(), 
                    message.replace("\"", "\\\""), 
                    status.value());

            return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(errorJson.getBytes())));
        };
    }
} 