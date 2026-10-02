package com.kevinmartinez.franchise.application.usecase.franchise;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.CreateFranchiseCommand;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.RenameFranchiseCommand;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.handler.CreateFranchiseHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.commands.handler.RenameFranchiseHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetFranchiseByIdQuery;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetFranchisesQuery;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.GetMaxStockProductsByFranchiseQuery;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.handler.GetFranchisesHandler;
import com.kevinmartinez.franchise.application.usecase.franchise.queries.handler.GetMaxStockProductsByFranchiseHandler;
import com.kevinmartinez.franchise.domain.model.Branch;
import com.kevinmartinez.franchise.domain.model.Franchise;
import com.kevinmartinez.franchise.domain.model.Product;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class FranchiseHandlersTest {
    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");

    @Test
    void createFranchiseNormalizesAndPersists() {
        InMemoryFranchiseRepository franchises = new InMemoryFranchiseRepository();
        StepVerifier.create(new CreateFranchiseHandler(franchises)
                        .handle(new CreateFranchiseCommand("  Acme  ")))
                .assertNext(franchise -> {
                    assertEquals("Acme", franchise.name());
                    assertFalse(franchise.id().isBlank());
                    assertNotNull(franchise.createdAt());
                    assertEquals(franchise.id(), franchises.saved.getId());
                }).verifyComplete();
        assertEquals(1, franchises.saveCalls);
    }

    @Test
    void invalidFranchiseNameFailsWithoutSaving() {
        InMemoryFranchiseRepository franchises = new InMemoryFranchiseRepository();
        StepVerifier.create(new CreateFranchiseHandler(franchises)
                        .handle(new CreateFranchiseCommand("  ")))
                .expectError(IllegalArgumentException.class).verify();
        assertEquals(0, franchises.saveCalls);
    }

    @Test
    void renameFranchisePersistsExistingFranchise() {
        InMemoryFranchiseRepository franchises = new InMemoryFranchiseRepository();
        franchises.data.put("fr-1", Franchise.create("fr-1", "Acme", NOW));
        StepVerifier.create(new RenameFranchiseHandler(franchises)
                        .handle(new RenameFranchiseCommand("fr-1", "Acme Colombia")))
                .assertNext(franchise -> assertEquals("Acme Colombia", franchise.name()))
                .verifyComplete();
        assertEquals(1, franchises.saveCalls);
    }

    @Test
    void renameMissingFranchiseReturnsNotFound() {
        StepVerifier.create(new RenameFranchiseHandler(new InMemoryFranchiseRepository())
                        .handle(new RenameFranchiseCommand("missing", "New name")))
                .expectError(ResourceNotFoundException.class).verify();
    }

    @Test
    void listFranchisesReturnsExistingFranchises() {
        InMemoryFranchiseRepository franchises = new InMemoryFranchiseRepository();
        franchises.data.put("fr-1", Franchise.create("fr-1", "Acme", NOW));
        franchises.data.put("fr-2", Franchise.create("fr-2", "Globex", NOW));
        StepVerifier.create(new GetFranchisesHandler(franchises).handle(new GetFranchisesQuery()))
                .expectNextCount(2).verifyComplete();
    }

    @Test
    void getFranchiseByIdReturnsExistingFranchise() {
        InMemoryFranchiseRepository franchises = new InMemoryFranchiseRepository();
        franchises.data.put("fr-1", Franchise.create("fr-1", "Acme", NOW));
        StepVerifier.create(new GetFranchisesHandler(franchises)
                        .handle(new GetFranchiseByIdQuery("fr-1")))
                .assertNext(franchise -> assertEquals("Acme", franchise.name()))
                .verifyComplete();
    }

    @Test
    void getFranchiseByMissingIdReturnsNotFound() {
        StepVerifier.create(new GetFranchisesHandler(new InMemoryFranchiseRepository())
                        .handle(new GetFranchiseByIdQuery("missing")))
                .expectError(ResourceNotFoundException.class).verify();
    }

    @Test
    void maxStockReturnsOneProductPerBranch() {
        InMemoryFranchiseRepository franchises = new InMemoryFranchiseRepository();
        InMemoryBranchRepository branches = new InMemoryBranchRepository();
        InMemoryProductRepository products = new InMemoryProductRepository();
        franchises.data.put("fr-1", Franchise.create("fr-1", "Acme", NOW));
        branches.data.put("br-1", Branch.create("br-1", "fr-1", "North", NOW));
        branches.data.put("br-2", Branch.create("br-2", "fr-1", "South", NOW));
        products.data.put("p-1", Product.create("p-1", "br-1", "Coffee", 10, NOW));
        products.data.put("p-2", Product.create("p-2", "br-1", "Tea", 25, NOW));
        products.data.put("p-3", Product.create("p-3", "br-2", "Water", 7, NOW));

        StepVerifier.create(new GetMaxStockProductsByFranchiseHandler(franchises, branches, products)
                        .handle(new GetMaxStockProductsByFranchiseQuery("fr-1")))
                .recordWith(java.util.ArrayList::new).expectNextCount(2)
                .consumeRecordedWith(results -> {
                    assertEquals("Tea", results.stream().filter(r -> r.branchId().equals("br-1"))
                            .findFirst().orElseThrow().productName());
                    assertEquals(7, results.stream().filter(r -> r.branchId().equals("br-2"))
                            .findFirst().orElseThrow().stock());
                }).verifyComplete();
    }

    @Test
    void maxStockMissingFranchiseReturnsNotFound() {
        StepVerifier.create(new GetMaxStockProductsByFranchiseHandler(
                        new InMemoryFranchiseRepository(), new InMemoryBranchRepository(),
                        new InMemoryProductRepository()).handle(
                                new GetMaxStockProductsByFranchiseQuery("missing")))
                .expectError(ResourceNotFoundException.class).verify();
    }

    private static final class InMemoryFranchiseRepository implements FranchiseRepository {
        private final Map<String, Franchise> data = new LinkedHashMap<>();
        private Franchise saved;
        private int saveCalls;
        public Mono<Franchise> save(Franchise value) { data.put(value.getId(), value); saved = value; saveCalls++; return Mono.just(value); }
        public Flux<Franchise> findAll() { return Flux.fromIterable(data.values()); }
        public Mono<Franchise> findById(String id) { return Mono.justOrEmpty(data.get(id)); }
    }

    private static final class InMemoryBranchRepository implements BranchRepository {
        private final Map<String, Branch> data = new LinkedHashMap<>();
        public Mono<Branch> save(Branch value) { data.put(value.getId(), value); return Mono.just(value); }
        public Flux<Branch> findAll() { return Flux.fromIterable(data.values()); }
        public Mono<Branch> findById(String id) { return Mono.justOrEmpty(data.get(id)); }
        public Flux<Branch> findByFranchiseId(String id) { return Flux.fromIterable(data.values()).filter(v -> v.getFranchiseId().equals(id)); }
    }

    private static final class InMemoryProductRepository implements ProductRepository {
        private final Map<String, Product> data = new LinkedHashMap<>();
        public Mono<Product> save(Product value) { data.put(value.getId(), value); return Mono.just(value); }
        public Flux<Product> findAll() { return Flux.fromIterable(data.values()); }
        public Mono<Product> findById(String id) { return Mono.justOrEmpty(data.get(id)); }
        public Flux<Product> findByBranchId(String id) { return Flux.fromIterable(data.values()).filter(v -> v.getBranchId().equals(id)); }
        public Mono<Void> deleteById(String id) { data.remove(id); return Mono.empty(); }
        public Mono<Product> findMaxStockByBranchId(String id) { return findByBranchId(id).sort((a, b) -> Integer.compare(b.getStock(), a.getStock())).next(); }
    }
}
