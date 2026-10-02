package com.kevinmartinez.franchise.application.usecase.product.commands.handler;

import java.util.Objects;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.application.usecase.product.commands.DeleteProductCommand;
import reactor.core.publisher.Mono;

public class DeleteProductHandler {

    private final ProductRepository productRepository;

    public DeleteProductHandler(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository,
                "El repositorio de productos no puede ser nulo");
    }

    public Mono<Void> handle(DeleteProductCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");
        return productRepository.findById(command.productId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró el producto con id: " + command.productId())))
                .flatMap(product -> productRepository.deleteById(product.getId()));
    }
}
