package com.kevinmartinez.franchise.application.usecase.product.commands.handler;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.application.usecase.product.commands.AddProductCommand;
import com.kevinmartinez.franchise.application.usecase.product.dto.ProductDto;
import com.kevinmartinez.franchise.domain.model.Product;
import reactor.core.publisher.Mono;

public class AddProductHandler {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public AddProductHandler(BranchRepository branchRepository, ProductRepository productRepository) {
        this.branchRepository = Objects.requireNonNull(branchRepository,
                "El repositorio de sucursales no puede ser nulo");
        this.productRepository = Objects.requireNonNull(productRepository,
                "El repositorio de productos no puede ser nulo");
    }

    public Mono<ProductDto> handle(AddProductCommand command) {
        Objects.requireNonNull(command, "El comando no puede ser nulo");
        return branchRepository.findById(command.branchId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No se encontró la sucursal con id: " + command.branchId())))
                .flatMap(branch -> productRepository.save(Product.create(
                        UUID.randomUUID().toString(), command.branchId(), command.name(), command.stock(), Instant.now())))
                .map(ProductDto::from);
    }
}
