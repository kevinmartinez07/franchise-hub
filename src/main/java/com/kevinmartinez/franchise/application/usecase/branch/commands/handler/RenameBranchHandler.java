package com.kevinmartinez.franchise.application.usecase.branch.commands.handler;

import java.time.Instant;
import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.usecase.branch.commands.RenameBranchCommand;
import com.kevinmartinez.franchise.application.usecase.branch.dto.BranchDto;
import reactor.core.publisher.Mono;

public class RenameBranchHandler {

    private final BranchRepository branchRepository;

    public RenameBranchHandler(BranchRepository branchRepository) {
        this.branchRepository = Objects.requireNonNull(branchRepository,
                "El repositorio de sucursales no puede ser nulo");
    }

    public Mono<BranchDto> handle(RenameBranchCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");
        return branchRepository.findById(command.branchId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la sucursal con id: " + command.branchId())))
                .flatMap(branch -> {
                    branch.rename(command.name(), Instant.now());
                    return branchRepository.save(branch);
                })
                .map(BranchDto::from);
    }
}
