package com.kevinmartinez.franchise.application.usecase.product.commands.handler;

import java.time.Instant;
import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.application.usecase.product.commands.RenameProductCommand;
import com.kevinmartinez.franchise.application.usecase.product.dto.ProductDto;
import reactor.core.publisher.Mono;

public class RenameProductHandler {

    private final ProductRepository productRepository;

    public RenameProductHandler(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository,
                "El repositorio de productos no puede ser nulo");
    }

    public Mono<ProductDto> handle(RenameProductCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");
        return productRepository.findById(command.productId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró el producto con id: " + command.productId())))
                .flatMap(product -> {
                    product.rename(command.name(), Instant.now());
                    return productRepository.save(product);
                })
                .map(ProductDto::from);
    }
}
