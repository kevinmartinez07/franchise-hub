package com.kevinmartinez.franchise.application.usecase.franchise.queries.handler;

import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.usecase.franchise.dto.FranchiseDto;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetFranchiseByIdQuery;
import reactor.core.publisher.Mono;

public class GetFranchiseByIdHandler {

    private final FranchiseRepository franchiseRepository;

    public GetFranchiseByIdHandler(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = Objects.requireNonNull(franchiseRepository);
    }

    public Mono<FranchiseDto> handle(GetFranchiseByIdQuery query) {
        Objects.requireNonNull(query, "La consulta no puede ser nula");
        return franchiseRepository.findById(query.franchiseId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la franquicia con id: " + query.franchiseId())))
                .map(FranchiseDto::from);
    }
}
