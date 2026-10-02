package com.kevinmartinez.franchise.application.usecase.product.commands.handler;

import java.time.Instant;
import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.application.usecase.product.commands.UpdateProductStockCommand;
import com.kevinmartinez.franchise.application.usecase.product.dto.ProductDto;
import reactor.core.publisher.Mono;

public class UpdateProductStockHandler {

    private final ProductRepository productRepository;

    public UpdateProductStockHandler(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository,
                "El repositorio de productos no puede ser nulo");
    }

    public Mono<ProductDto> handle(UpdateProductStockCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");
        return productRepository.findById(command.productId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró el producto con id: " + command.productId())))
                .flatMap(product -> {
                    product.updateStock(command.stock(), Instant.now());
                    return productRepository.save(product);
                })
                .map(ProductDto::from);
    }
}
