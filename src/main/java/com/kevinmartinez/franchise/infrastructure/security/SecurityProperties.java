package com.kevinmartinez.franchise.infrastructure.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        Jwt jwt,
        DemoUser demoUser) {

    public record Jwt(
            String secret,
            String issuer,
            String audience,
            Duration expiration) {
    }

    public record DemoUser(
            String username,
            String password) {
    }
}
