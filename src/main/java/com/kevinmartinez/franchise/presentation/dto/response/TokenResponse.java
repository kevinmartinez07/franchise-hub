package com.kevinmartinez.franchise.presentation.dto.response;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn) {
}
