package com.kevinmartinez.franchise.application.usecase.branch.commands.handler;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.usecase.branch.commands.AddBranchCommand;
import com.kevinmartinez.franchise.application.usecase.branch.dto.BranchDto;
import com.kevinmartinez.franchise.domain.model.Branch;
import reactor.core.publisher.Mono;

public class AddBranchHandler {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;

    public AddBranchHandler(FranchiseRepository franchiseRepository, BranchRepository branchRepository) {
        this.franchiseRepository = Objects.requireNonNull(franchiseRepository,
                "El repositorio de franquicias no puede ser nulo");
        this.branchRepository = Objects.requireNonNull(branchRepository,
                "El repositorio de sucursales no puede ser nulo");
    }

    public Mono<BranchDto> handle(AddBranchCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");
        return franchiseRepository.findById(command.franchiseId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la franquicia con id: " + command.franchiseId())))
                .flatMap(franchise -> branchRepository.save(Branch.create(
                        UUID.randomUUID().toString(), command.franchiseId(), command.name(), Instant.now())))
                .map(BranchDto::from);
    }
}
