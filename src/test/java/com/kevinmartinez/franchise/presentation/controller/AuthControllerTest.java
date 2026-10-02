package com.kevinmartinez.franchise.presentation.controller;

import static org.mockito.Mockito.when;

import com.kevinmartinez.franchise.application.security.AccessToken;
import com.kevinmartinez.franchise.application.security.AuthenticationService;
import com.kevinmartinez.franchise.presentation.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(AuthController.class)
@Import({GlobalExceptionHandler.class, AuthControllerTest.TestSecurityConfig.class})
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    void loginReturnsTokenForValidCredentials() {
        when(authenticationService.authenticate("reviewer", "secret"))
                .thenReturn(Mono.just(new AccessToken("token", 1800)));

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"reviewer\",\"password\":\"secret\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.accessToken").isEqualTo("token")
                .jsonPath("$.data.tokenType").isEqualTo("Bearer");
    }

    @Test
    void loginReturns401ForInvalidCredentials() {
        when(authenticationService.authenticate("reviewer", "wrong"))
                .thenReturn(Mono.error(new org.springframework.security.authentication.BadCredentialsException(
                        "Usuario o contraseña inválidos")));

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"username\":\"reviewer\",\"password\":\"wrong\"}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        SecurityWebFilterChain testSecurityWebFilterChain(ServerHttpSecurity http) {
            return http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                    .build();
        }
    }
}
