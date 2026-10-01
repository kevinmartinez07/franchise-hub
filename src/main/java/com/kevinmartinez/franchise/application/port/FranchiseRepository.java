package com.kevinmartinez.franchise.application.port;

import com.kevinmartinez.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {

    Mono<Franchise> save(Franchise franchise);
}
