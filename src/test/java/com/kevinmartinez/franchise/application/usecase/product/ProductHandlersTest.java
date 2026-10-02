package com.kevinmartinez.franchise.application.usecase.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.application.usecase.product.commands.AddProductCommand;
import com.kevinmartinez.franchise.application.usecase.product.commands.DeleteProductCommand;
import com.kevinmartinez.franchise.application.usecase.product.commands.RenameProductCommand;
import com.kevinmartinez.franchise.application.usecase.product.commands.UpdateProductStockCommand;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.AddProductHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.DeleteProductHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.RenameProductHandler;
import com.kevinmartinez.franchise.application.usecase.product.commands.handler.UpdateProductStockHandler;
import com.kevinmartinez.franchise.application.usecase.product.queries.GetProductByIdQuery;
import com.kevinmartinez.franchise.application.usecase.product.queries.GetProductsQuery;
import com.kevinmartinez.franchise.application.usecase.product.queries.handler.GetProductsHandler;
import com.kevinmartinez.franchise.domain.model.Branch;
import com.kevinmartinez.franchise.domain.model.Product;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ProductHandlersTest {
    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");

    @Test
    void addProductChecksBranchAndPersistsProduct() {
        BranchFake branches = new BranchFake(); ProductFake products = new ProductFake();
        branches.data.put("br-1", Branch.create("br-1", "fr-1", "North", NOW));
        StepVerifier.create(new AddProductHandler(branches, products).handle(new AddProductCommand("br-1", "Coffee", 10)))
                .assertNext(product -> {
                    assertEquals("br-1", product.branchId());
                    assertEquals(10, product.stock());
                    assertFalse(product.id().isBlank());
                })
                .verifyComplete();
        assertEquals(1, products.saveCalls);
    }

    @Test
    void addProductRejectsNegativeStockWithoutSaving() {
        BranchFake branches = new BranchFake(); ProductFake products = new ProductFake();
        branches.data.put("br-1", Branch.create("br-1", "fr-1", "North", NOW));
        StepVerifier.create(new AddProductHandler(branches, products).handle(new AddProductCommand("br-1", "Coffee", -1)))
                .expectError(IllegalArgumentException.class).verify();
        assertEquals(0, products.saveCalls);
    }

    @Test
    void addProductReturnsNotFoundWhenBranchDoesNotExist() {
        ProductFake products = new ProductFake();
        StepVerifier.create(new AddProductHandler(new BranchFake(), products).handle(new AddProductCommand("missing", "Coffee", 1)))
                .expectError(ResourceNotFoundException.class).verify();
        assertEquals(0, products.saveCalls);
    }

    @Test
    void deleteExistingProductRemovesIt() {
        ProductFake products = new ProductFake(); products.data.put("p-1", Product.create("p-1", "br-1", "Coffee", 5, NOW));
        StepVerifier.create(new DeleteProductHandler(products).handle(new DeleteProductCommand("p-1"))).verifyComplete();
        assertFalse(products.data.containsKey("p-1")); assertEquals(1, products.deleteCalls);
    }

    @Test
    void deleteMissingProductReturnsNotFoundAndDoesNotDelete() {
        ProductFake products = new ProductFake();
        StepVerifier.create(new DeleteProductHandler(products).handle(new DeleteProductCommand("missing")))
                .expectError(ResourceNotFoundException.class).verify();
        assertEquals(0, products.deleteCalls);
    }

    @Test
    void updateStockPersistsDomainChange() {
        ProductFake products = new ProductFake(); products.data.put("p-1", Product.create("p-1", "br-1", "Coffee", 5, NOW));
        StepVerifier.create(new UpdateProductStockHandler(products).handle(new UpdateProductStockCommand("p-1", 20)))
                .assertNext(product -> assertEquals(20, product.stock())).verifyComplete();
        assertEquals(1, products.saveCalls);
    }

    @Test
    void updateNegativeStockFailsWithoutSaving() {
        ProductFake products = new ProductFake(); products.data.put("p-1", Product.create("p-1", "br-1", "Coffee", 5, NOW));
        StepVerifier.create(new UpdateProductStockHandler(products).handle(new UpdateProductStockCommand("p-1", -1)))
                .expectError(IllegalArgumentException.class).verify();
        assertEquals(0, products.saveCalls);
    }

    @Test
    void renameProductPersistsChange() {
        ProductFake products = new ProductFake(); products.data.put("p-1", Product.create("p-1", "br-1", "Coffee", 5, NOW));
        StepVerifier.create(new RenameProductHandler(products).handle(new RenameProductCommand("p-1", "Espresso")))
                .assertNext(product -> assertEquals("Espresso", product.name())).verifyComplete();
    }

    @Test
    void missingProductReturnsNotFoundForUpdateAndRename() {
        ProductFake products = new ProductFake();
        StepVerifier.create(new UpdateProductStockHandler(products).handle(new UpdateProductStockCommand("missing", 1)))
                .expectError(ResourceNotFoundException.class).verify();
        StepVerifier.create(new RenameProductHandler(products).handle(new RenameProductCommand("missing", "Espresso")))
                .expectError(ResourceNotFoundException.class).verify();
    }

    @Test
    void listProductsReturnsAllProducts() {
        ProductFake products = new ProductFake();
        products.data.put("p-1", Product.create("p-1", "br-1", "Coffee", 5, NOW));
        products.data.put("p-2", Product.create("p-2", "br-2", "Tea", 10, NOW));
        StepVerifier.create(new GetProductsHandler(products, new BranchFake()).handle(new GetProductsQuery(null)))
                .expectNextCount(2).verifyComplete();
    }

    @Test
    void listProductsByBranchReturnsOnlyMatchingProducts() {
        BranchFake branches = new BranchFake(); ProductFake products = new ProductFake();
        branches.data.put("br-1", Branch.create("br-1", "fr-1", "North", NOW));
        products.data.put("p-1", Product.create("p-1", "br-1", "Coffee", 5, NOW));
        products.data.put("p-2", Product.create("p-2", "br-2", "Tea", 10, NOW));
        StepVerifier.create(new GetProductsHandler(products, branches).handle(new GetProductsQuery("br-1")))
                .assertNext(product -> assertEquals("p-1", product.id())).verifyComplete();
    }

    @Test
    void listProductsByMissingBranchReturnsNotFound() {
        StepVerifier.create(new GetProductsHandler(new ProductFake(), new BranchFake()).handle(new GetProductsQuery("missing")))
                .expectError(ResourceNotFoundException.class).verify();
    }

    @Test
    void listProductsByExistingBranchWithoutProductsReturnsEmpty() {
        BranchFake branches = new BranchFake(); branches.data.put("br-1", Branch.create("br-1", "fr-1", "North", NOW));
        StepVerifier.create(new GetProductsHandler(new ProductFake(), branches).handle(new GetProductsQuery("br-1")))
                .verifyComplete();
    }

    @Test
    void getProductByIdReturnsExistingProduct() {
        ProductFake products = new ProductFake(); products.data.put("p-1", Product.create("p-1", "br-1", "Coffee", 5, NOW));
        StepVerifier.create(new GetProductsHandler(products, new BranchFake()).handle(new GetProductByIdQuery("p-1")))
                .assertNext(product -> assertEquals("Coffee", product.name())).verifyComplete();
    }

    @Test
    void getProductByMissingIdReturnsNotFound() {
        StepVerifier.create(new GetProductsHandler(new ProductFake(), new BranchFake()).handle(new GetProductByIdQuery("missing")))
                .expectError(ResourceNotFoundException.class).verify();
    }

    private static final class BranchFake implements BranchRepository {
        private final Map<String, Branch> data = new LinkedHashMap<>();
        public Mono<Branch> save(Branch value) { data.put(value.getId(), value); return Mono.just(value); }
        public Flux<Branch> findAll() { return Flux.fromIterable(data.values()); }
        public Mono<Branch> findById(String id) { return Mono.justOrEmpty(data.get(id)); }
        public Flux<Branch> findByFranchiseId(String id) {
            return Flux.fromIterable(data.values()).filter(v -> v.getFranchiseId().equals(id));
        }
    }

    private static final class ProductFake implements ProductRepository {
        private final Map<String, Product> data = new LinkedHashMap<>(); private int saveCalls; private int deleteCalls;
        public Mono<Product> save(Product value) { saveCalls++; data.put(value.getId(), value); return Mono.just(value); }
        public Flux<Product> findAll() { return Flux.fromIterable(data.values()); }
        public Mono<Product> findById(String id) { return Mono.justOrEmpty(data.get(id)); }
        public Flux<Product> findByBranchId(String id) { return Flux.fromIterable(data.values()).filter(v -> v.getBranchId().equals(id)); }
        public Mono<Void> deleteById(String id) { deleteCalls++; data.remove(id); return Mono.empty(); }
        public Mono<Product> findMaxStockByBranchId(String id) {
            return findByBranchId(id)
                    .sort((a, b) -> Integer.compare(b.getStock(), a.getStock()))
                    .next();
        }
    }
}
