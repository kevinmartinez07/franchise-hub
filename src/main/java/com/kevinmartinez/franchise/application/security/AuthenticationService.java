package com.kevinmartinez.franchise.application.security;

import reactor.core.publisher.Mono;

public interface AuthenticationService {

    Mono<AccessToken> authenticate(String username, String password);
}
