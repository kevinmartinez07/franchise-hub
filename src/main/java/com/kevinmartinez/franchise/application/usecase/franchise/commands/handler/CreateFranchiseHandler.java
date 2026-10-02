package com.kevinmartinez.franchise.application.usecase.franchise.commands.handler;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.CreateFranchiseCommand;
import com.kevinmartinez.franchise.application.usecase.franchise.dto.FranchiseDto;
import com.kevinmartinez.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;

public class CreateFranchiseHandler {

    private final FranchiseRepository franchiseRepository;

    public CreateFranchiseHandler(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = Objects.requireNonNull(
                franchiseRepository,
                "El repositorio de franquicias no puede ser nulo");
    }

    public Mono<FranchiseDto> handle(CreateFranchiseCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");

        return Mono.defer(() -> {
            Franchise franchise = Franchise.create(
                    UUID.randomUUID().toString(),
                    command.name(),
                    Instant.now());

            return franchiseRepository.save(franchise)
                    .map(FranchiseDto::from);
        });
    }
}
