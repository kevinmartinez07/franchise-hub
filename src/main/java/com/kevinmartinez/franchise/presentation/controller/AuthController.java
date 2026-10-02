package com.kevinmartinez.franchise.presentation.controller;

import com.kevinmartinez.franchise.application.security.AuthenticationService;
import com.kevinmartinez.franchise.presentation.dto.request.LoginRequest;
import com.kevinmartinez.franchise.presentation.dto.response.ApiResponse;
import com.kevinmartinez.franchise.presentation.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener un JWT para usar la API")
    public Mono<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.authenticate(request.username(), request.password())
                .map(token -> new TokenResponse(token.value(), "Bearer", token.expiresInSeconds()))
                .map(response -> ApiResponse.success("Autenticación exitosa", response));
    }
}
