package com.kevinmartinez.franchise.application.repository;

import com.kevinmartinez.franchise.domain.model.Franchise;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {

    Mono<Franchise> save(Franchise franchise);

    Flux<Franchise> findAll();

    Mono<Franchise> findById(String id);
}
