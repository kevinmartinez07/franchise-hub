package com.kevinmartinez.franchise.infrastructure.security;

import com.kevinmartinez.franchise.application.security.AccessToken;
import com.kevinmartinez.franchise.application.security.AuthenticationService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class DemoAuthenticationService implements AuthenticationService {

    private final SecurityProperties securityProperties;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final String encodedPassword;

    public DemoAuthenticationService(
            SecurityProperties securityProperties,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService) {
        this.securityProperties = securityProperties;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.encodedPassword = passwordEncoder.encode(securityProperties.demoUser().password());
    }

    @Override
    public Mono<AccessToken> authenticate(String username, String password) {
        return Mono.fromCallable(() -> {
                    boolean validUsername = securityProperties.demoUser().username().equals(username);
                    boolean validPassword = passwordEncoder.matches(password, encodedPassword);

                    if (!validUsername || !validPassword) {
                        throw new BadCredentialsException("Usuario o contraseña inválidos");
                    }

                    return jwtTokenService.issue(username);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
