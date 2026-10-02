package com.kevinmartinez.franchise.infrastructure.persistence.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.repository.ProductRepository;
import com.kevinmartinez.franchise.domain.model.Branch;
import com.kevinmartinez.franchise.domain.model.Franchise;
import com.kevinmartinez.franchise.domain.model.Product;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.adapter.MongoBranchRepositoryAdapter;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.adapter.MongoFranchiseRepositoryAdapter;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.adapter.MongoProductRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataMongoTest
@Import({
        MongoFranchiseRepositoryAdapter.class,
        MongoBranchRepositoryAdapter.class,
        MongoProductRepositoryAdapter.class
})
@Testcontainers(disabledWithoutDocker = true)
class MongoRepositoryAdaptersIntegrationTest {

    @Container
    static final MongoDBContainer MONGODB = new MongoDBContainer(
            DockerImageName.parse(
                    System.getenv().getOrDefault(
                            "TEST_MONGO_IMAGE",
                            "public.ecr.aws/docker/library/mongo:8.0"))
                    .asCompatibleSubstituteFor("mongo"));

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGODB::getReplicaSetUrl);
    }

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final ReactiveMongoTemplate mongoTemplate;

    @Autowired
    MongoRepositoryAdaptersIntegrationTest(
            FranchiseRepository franchiseRepository,
            BranchRepository branchRepository,
            ProductRepository productRepository,
            ReactiveMongoTemplate mongoTemplate) {
        this.franchiseRepository = franchiseRepository;
        this.branchRepository = branchRepository;
        this.productRepository = productRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @BeforeEach
    void cleanCollections() {
        StepVerifier.create(Mono.when(
                        dropIfExists("franchises"),
                        dropIfExists("branches"),
                        dropIfExists("products")))
                .verifyComplete();
    }

    @Test
    void persistsAndFindsFranchiseById() {
        String franchiseId = id();
        Franchise franchise = Franchise.create(franchiseId, "Acme", now());

        StepVerifier.create(franchiseRepository.save(franchise).then(franchiseRepository.findById(franchiseId)))
                .assertNext(found -> {
                    assertEquals(franchiseId, found.getId());
                    assertEquals("Acme", found.getName());
                })
                .verifyComplete();
    }

    @Test
    void persistsBranchAndFindsBranchesByFranchise() {
        String franchiseId = id();
        Branch north = Branch.create(id(), franchiseId, "North", now());
        Branch south = Branch.create(id(), franchiseId, "South", now());

        StepVerifier.create(
                        franchiseRepository.save(Franchise.create(franchiseId, "Acme", now()))
                                .then(branchRepository.save(north))
                                .then(branchRepository.save(south))
                                .then(branchRepository.findByFranchiseId(franchiseId).collectList()))
                .assertNext(branches -> {
                    assertEquals(2, branches.size());
                    assertTrue(branches.stream().allMatch(branch -> franchiseId.equals(branch.getFranchiseId())));
                })
                .verifyComplete();
    }

    @Test
    void persistsProductAndFindsById() {
        String branchId = id();
        Product product = Product.create(id(), branchId, "Coffee", 15, now());

        StepVerifier.create(
                        branchRepository.save(Branch.create(branchId, id(), "North", now()))
                                .then(productRepository.save(product))
                                .then(productRepository.findById(product.getId())))
                .assertNext(found -> {
                    assertEquals(product.getId(), found.getId());
                    assertEquals(branchId, found.getBranchId());
                    assertEquals("Coffee", found.getName());
                    assertEquals(15, found.getStock());
                })
                .verifyComplete();
    }

    @Test
    void findsMaxStockProductByBranch() {
        String branchId = id();
        Product low = Product.create(id(), branchId, "Coffee", 10, now());
        Product high = Product.create(id(), branchId, "Tea", 25, now());

        StepVerifier.create(
                        branchRepository.save(Branch.create(branchId, id(), "North", now()))
                                .then(productRepository.save(low))
                                .then(productRepository.save(high))
                                .then(productRepository.findMaxStockByBranchId(branchId)))
                .assertNext(found -> assertEquals(high.getId(), found.getId()))
                .verifyComplete();
    }

    @Test
    void deletesProduct() {
        Product product = Product.create(id(), id(), "Coffee", 10, now());

        StepVerifier.create(
                        productRepository.save(product)
                                .then(productRepository.deleteById(product.getId()))
                                .then(productRepository.findById(product.getId())))
                .verifyComplete();
    }

    private Mono<Void> dropIfExists(String collection) {
        return mongoTemplate.collectionExists(collection)
                .flatMap(exists -> exists ? mongoTemplate.dropCollection(collection) : Mono.empty());
    }

    private static String id() {
        return UUID.randomUUID().toString();
    }

    private static Instant now() {
        return Instant.parse("2026-10-01T12:00:00Z");
    }
}
