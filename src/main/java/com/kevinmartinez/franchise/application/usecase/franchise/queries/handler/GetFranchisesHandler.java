package com.kevinmartinez.franchise.application.usecase.franchise.queries.handler;

import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.usecase.franchise.dto.FranchiseDto;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetFranchiseByIdQuery;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetFranchisesQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GetFranchisesHandler {

    private final FranchiseRepository franchiseRepository;

    public GetFranchisesHandler(FranchiseRepository franchiseRepository) {
        this.franchiseRepository = Objects.requireNonNull(franchiseRepository);
    }

    public Flux<FranchiseDto> handle(GetFranchisesQuery query) {
        Objects.requireNonNull(query, "La consulta no puede ser nula");
        return franchiseRepository.findAll().map(FranchiseDto::from);
    }

    public Mono<FranchiseDto> handle(GetFranchiseByIdQuery query) {
        Objects.requireNonNull(query, "La consulta no puede ser nula");
        return franchiseRepository.findById(query.franchiseId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la franquicia con id: " + query.franchiseId())))
                .map(FranchiseDto::from);
    }
}
