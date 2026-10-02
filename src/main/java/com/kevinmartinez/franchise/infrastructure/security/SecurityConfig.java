package com.kevinmartinez.franchise.infrastructure.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            SecurityProblemWriter problemWriter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .pathMatchers(
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**")
                        .permitAll()
                        .anyExchange()
                        .authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint((exchange, exception) -> problemWriter.write(
                                exchange,
                                HttpStatus.UNAUTHORIZED,
                                "No autenticado",
                                "Se requiere un token válido para acceder al recurso",
                                "UNAUTHORIZED")))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, exception) -> problemWriter.write(
                                exchange,
                                HttpStatus.UNAUTHORIZED,
                                "No autenticado",
                                "Se requiere autenticación para acceder al recurso",
                                "UNAUTHORIZED"))
                        .accessDeniedHandler((exchange, exception) -> problemWriter.write(
                                exchange,
                                HttpStatus.FORBIDDEN,
                                "Acceso denegado",
                                "No tienes permiso para acceder al recurso",
                                "FORBIDDEN")))
                .build();
    }

    @Bean
    SecretKey jwtSecretKey(SecurityProperties properties) {
        byte[] secret = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes");
        }
        return new SecretKeySpec(secret, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<SecurityContext>(secretKey));
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder(SecretKey secretKey, SecurityProperties properties) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> defaults =
                JwtValidators.createDefaultWithIssuer(properties.jwt().issuer());
        OAuth2TokenValidator<Jwt> audience =
                new JwtAudienceValidator(properties.jwt().audience());
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaults, audience));
        return decoder;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    JwtTokenService jwtTokenService(JwtEncoder jwtEncoder, SecurityProperties properties) {
        return new JwtTokenService(jwtEncoder, properties);
    }

    @Bean
    DemoAuthenticationService demoAuthenticationService(
            SecurityProperties properties,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        return new DemoAuthenticationService(properties, passwordEncoder, jwtTokenService);
    }

    @Bean
    SecurityProblemWriter securityProblemWriter(ObjectMapper objectMapper) {
        return new SecurityProblemWriter(objectMapper);
    }
}
