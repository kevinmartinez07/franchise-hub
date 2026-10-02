package com.kevinmartinez.franchise.application.usecase.branch.queries.handler;

import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.usecase.branch.dto.BranchDto;
import com.kevinmartinez.franchise.application.usecase.branch.queries.GetBranchesQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GetBranchesHandler {

    private final BranchRepository branchRepository;
    private final FranchiseRepository franchiseRepository;

    public GetBranchesHandler(BranchRepository branchRepository, FranchiseRepository franchiseRepository) {
        this.branchRepository = Objects.requireNonNull(branchRepository);
        this.franchiseRepository = Objects.requireNonNull(franchiseRepository);
    }

    public Flux<BranchDto> handle(GetBranchesQuery query) {
        Objects.requireNonNull(query, "La consulta no puede ser nula");
        if (query.franchiseId() == null || query.franchiseId().isBlank()) {
            return branchRepository.findAll().map(BranchDto::from);
        }
        return franchiseRepository.findById(query.franchiseId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la franquicia con id: " + query.franchiseId())))
                .flatMapMany(franchise -> branchRepository.findByFranchiseId(query.franchiseId()))
                .map(BranchDto::from);
    }
}
