package com.kevinmartinez.franchise.application.usecase;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.kevinmartinez.franchise.application.port.FranchiseRepository;
import com.kevinmartinez.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

public class CreateFranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public CreateFranchiseUseCase(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = Objects.requireNonNull(franchiseRepository, "El repositorio de franquicias no puede ser nulo");
    }

    public Mono<Franchise> execute(String name) {
        return Mono.defer(() -> {
            Franchise franchise = Franchise.create(UUID.randomUUID().toString(), name, Instant.now());
            return franchiseRepository.save(franchise);
        });
    }
}
