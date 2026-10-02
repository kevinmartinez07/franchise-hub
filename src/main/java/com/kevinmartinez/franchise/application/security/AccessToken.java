package com.kevinmartinez.franchise.application.security;

public record AccessToken(
        String value,
        long expiresInSeconds) {
}
