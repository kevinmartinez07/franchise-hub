package com.kevinmartinez.franchise.application.usecase.product.queries.handler;

import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.application.usecase.product.dto.ProductDto;
import com.kevinmartinez.franchise.application.usecase.product.queries.GetProductByIdQuery;
import com.kevinmartinez.franchise.application.usecase.product.queries.GetProductsQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GetProductsHandler {

    private final ProductRepository productRepository;
    private final BranchRepository branchRepository;

    public GetProductsHandler(ProductRepository productRepository, BranchRepository branchRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.branchRepository = Objects.requireNonNull(branchRepository);
    }

    public Flux<ProductDto> handle(GetProductsQuery query) {
        Objects.requireNonNull(query, "La consulta no puede ser nula");
        if (query.branchId() == null || query.branchId().isBlank()) {
            return productRepository.findAll().map(ProductDto::from);
        }
        return branchRepository.findById(query.branchId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la sucursal con id: " + query.branchId())))
                .flatMapMany(branch -> productRepository.findByBranchId(query.branchId()))
                .map(ProductDto::from);
    }

    public Mono<ProductDto> handle(GetProductByIdQuery query) {
        Objects.requireNonNull(query, "La consulta no puede ser nula");
        return productRepository.findById(query.productId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró el producto con id: " + query.productId())))
                .map(ProductDto::from);
    }
}
