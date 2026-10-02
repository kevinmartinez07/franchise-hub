package com.kevinmartinez.franchise.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.test.StepVerifier;

class JwtSecurityTest {

    private static final String SECRET = "test-only-franchise-hub-secret-with-more-than-32-bytes";

    @Test
    void issuedTokenCanBeDecodedWithExpectedClaims() {
        SecurityProperties properties = properties();
        SecurityConfig config = new SecurityConfig();
        var key = config.jwtSecretKey(properties);
        var decoder = config.jwtDecoder(key, properties);
        var tokenService = new JwtTokenService(config.jwtEncoder(key), properties);

        var issued = tokenService.issue("reviewer");

        StepVerifier.create(decoder.decode(issued.value()))
                .assertNext(jwt -> {
                    assertEquals("reviewer", jwt.getSubject());
                    assertEquals("franchise-hub", jwt.getClaimAsString("iss"));
                    assertTrue(jwt.getAudience().contains("franchise-hub-api"));
                    assertTrue(jwt.getClaims().containsKey("jti"));
                })
                .verifyComplete();

        assertEquals(1800, issued.expiresInSeconds());
    }

    @Test
    void demoAuthenticationRejectsInvalidPassword() {
        SecurityProperties properties = properties();
        SecurityConfig config = new SecurityConfig();
        var key = config.jwtSecretKey(properties);
        var authenticationService = new DemoAuthenticationService(
                properties,
                config.passwordEncoder(),
                new JwtTokenService(config.jwtEncoder(key), properties));

        StepVerifier.create(authenticationService.authenticate("reviewer", "wrong"))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void decoderRejectsTokenWithUnexpectedAudience() {
        SecurityProperties expected = properties();
        SecurityConfig config = new SecurityConfig();
        var key = config.jwtSecretKey(expected);
        var wrongAudience = new SecurityProperties(
                new SecurityProperties.Jwt(SECRET, "franchise-hub", "another-api", Duration.ofMinutes(30)),
                expected.demoUser());

        var token = new JwtTokenService(config.jwtEncoder(key), wrongAudience).issue("reviewer");

        StepVerifier.create(config.jwtDecoder(key, expected).decode(token.value()))
                .expectError()
                .verify();
    }

    private static SecurityProperties properties() {
        return new SecurityProperties(
                new SecurityProperties.Jwt(SECRET, "franchise-hub", "franchise-hub-api", Duration.ofMinutes(30)),
                new SecurityProperties.DemoUser("reviewer", "test-only-password"));
    }
}
