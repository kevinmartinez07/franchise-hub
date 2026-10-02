package com.kevinmartinez.franchise.application.usecase.branch.queries.handler;

import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.usecase.branch.dto.BranchDto;
import com.kevinmartinez.franchise.application.usecase.branch.queries.GetBranchByIdQuery;
import reactor.core.publisher.Mono;

public class GetBranchByIdHandler {

    private final BranchRepository branchRepository;

    public GetBranchByIdHandler(BranchRepository branchRepository) {
        this.branchRepository = Objects.requireNonNull(branchRepository);
    }

    public Mono<BranchDto> handle(GetBranchByIdQuery query) {
        Objects.requireNonNull(query, "La consulta no puede ser nula");
        return branchRepository.findById(query.branchId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la sucursal con id: " + query.branchId())))
                .map(BranchDto::from);
    }
}
