package com.kevinmartinez.franchise.application.usecase.franchise.queries.handler;

import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.application.usecase.franchise.dto.MaxStockProductDto;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetMaxStockProductsByFranchiseQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GetMaxStockProductsByFranchiseHandler {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public GetMaxStockProductsByFranchiseHandler(
            FranchiseRepository franchiseRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository) {
        this.franchiseRepository = Objects.requireNonNull(franchiseRepository,
                "El repositorio de franquicias no puede ser nulo");
        this.branchRepository = Objects.requireNonNull(branchRepository,
                "El repositorio de sucursales no puede ser nulo");
        this.productRepository = Objects.requireNonNull(productRepository,
                "El repositorio de productos no puede ser nulo");
    }

    public Flux<MaxStockProductDto> handle(GetMaxStockProductsByFranchiseQuery query) {
        Objects.requireNonNull(query, "La consulta no puede ser nula");

        return franchiseRepository.findById(query.franchiseId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la franquicia con id: " + query.franchiseId())))
                .flatMapMany(franchise -> branchRepository.findByFranchiseId(query.franchiseId()))
                .flatMap(branch -> productRepository.findMaxStockByBranchId(branch.getId())
                        .map(product -> new MaxStockProductDto(
                                branch.getId(),
                                branch.getName(),
                                product.getId(),
                                product.getName(),
                                product.getStock())));
    }
}
