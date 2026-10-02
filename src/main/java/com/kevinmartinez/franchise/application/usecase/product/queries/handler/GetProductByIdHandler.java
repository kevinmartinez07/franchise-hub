package com.kevinmartinez.franchise.application.usecase.product.queries.handler;

import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.application.usecase.product.dto.ProductDto;
import com.kevinmartinez.franchise.application.usecase.product.queries.GetProductByIdQuery;
import reactor.core.publisher.Mono;

public class GetProductByIdHandler {

    private final ProductRepository productRepository;

    public GetProductByIdHandler(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    public Mono<ProductDto> handle(GetProductByIdQuery query) {
        Objects.requireNonNull(query, "La consulta no puede ser nula");
        return productRepository.findById(query.productId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró el producto con id: " + query.productId())))
                .map(ProductDto::from);
    }
}
