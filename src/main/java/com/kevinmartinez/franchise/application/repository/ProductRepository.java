package com.kevinmartinez.franchise.application.repository;

import com.kevinmartinez.franchise.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductRepository {

    Mono<Product> save(Product product);

    Flux<Product> findAll();

    Mono<Product> findById(String id);

    Flux<Product> findByBranchId(String branchId);

    Mono<Void> deleteById(String id);

    Mono<Product> findMaxStockByBranchId(String branchId);
}
