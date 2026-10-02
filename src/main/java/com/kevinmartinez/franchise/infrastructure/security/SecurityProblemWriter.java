package com.kevinmartinez.franchise.infrastructure.security;

import java.net.URI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class SecurityProblemWriter {

    private final ObjectMapper objectMapper;

    public SecurityProblemWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> write(
            ServerWebExchange exchange,
            HttpStatus status,
            String title,
            String detail,
            String code) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("urn:franchise-hub:error:" + code.toLowerCase()));
        problem.setInstance(exchange.getRequest().getURI());
        problem.setProperty("code", code);

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(problem);
        } catch (JsonProcessingException exception) {
            return Mono.error(exception);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}
