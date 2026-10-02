package com.kevinmartinez.franchise.application.usecase.franchise.commands.handler;

import java.time.Instant;
import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.RenameFranchiseCommand;
import com.kevinmartinez.franchise.application.usecase.franchise.dto.FranchiseDto;
import reactor.core.publisher.Mono;

public class RenameFranchiseHandler {

    private final FranchiseRepository franchiseRepository;

    public RenameFranchiseHandler(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = Objects.requireNonNull(
                franchiseRepository,
                "El repositorio de franquicias no puede ser nulo");
    }

    public Mono<FranchiseDto> handle(RenameFranchiseCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");

        return franchiseRepository.findById(command.franchiseId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la franquicia con id: " + command.franchiseId())))
                .flatMap(franchise -> {
                    franchise.rename(command.name(), Instant.now());
                    return franchiseRepository.save(franchise);
                })
                .map(FranchiseDto::from);
    }
}
