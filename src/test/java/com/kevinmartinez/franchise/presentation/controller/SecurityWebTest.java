package com.kevinmartinez.franchise.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;

import com.kevinmartinez.franchise.application.usecase.franchise.commands.handler.CreateFranchiseHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.handler.RenameFranchiseHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.dto.FranchiseDto;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetFranchisesQuery;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.handler.GetFranchiseByIdHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.handler.GetFranchisesHandler;
import com.kevinmartinez.franchise.infrastructure.security.JwtTokenService;
import com.kevinmartinez.franchise.infrastructure.security.SecurityConfig;
import com.kevinmartinez.franchise.presentation.error.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
@WebFluxTest(controllers = FranchiseController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "JWT_SECRET=test-only-franchise-hub-secret-with-more-than-32-bytes",
        "FRANCHISE_APP_USERNAME=reviewer",
        "FRANCHISE_APP_PASSWORD=test-only-password"
})
class SecurityWebTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CreateFranchiseHandler createFranchiseHandler;

    @MockitoBean
    private RenameFranchiseHandler renameFranchiseHandler;

    @MockitoBean
    private GetFranchisesHandler getFranchisesHandler;

    @MockitoBean
    private GetFranchiseByIdHandler getFranchiseByIdHandler;

    @Test
    void protectedEndpointWithoutBearerReturns401() {
        webTestClient.get()
                .uri("/api/franchises")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void protectedEndpointWithInvalidBearerReturns401() {
        webTestClient.get()
                .uri("/api/franchises")
                .headers(headers -> headers.setBearerAuth("invalid-token"))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.code").isEqualTo("UNAUTHORIZED");
    }

    @Test
    void protectedEndpointWithValidJwtPassesSecurity() {
        Instant now = Instant.parse("2026-10-01T12:00:00Z");
        when(getFranchisesHandler.handle(any(GetFranchisesQuery.class)))
                .thenReturn(reactor.core.publisher.Flux.just(new FranchiseDto("fr-1", "Acme", now, now)));

        webTestClient.get()
                .uri("/api/franchises")
                .headers(headers -> headers.setBearerAuth(jwtTokenService.issue("reviewer").value()))
                .exchange()
                .expectStatus().isOk();
    }
}
