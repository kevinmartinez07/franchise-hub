package com.kevinmartinez.franchise;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.repository.ReactiveBranchMongoRepository;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.repository.ReactiveFranchiseMongoRepository;
import com.kevinmartinez.franchise.infrastructure.persistence.mongo.repository.ReactiveProductMongoRepository;

@SpringBootTest(properties = {
        "FRANCHISE_APP_USERNAME=reviewer",
        "FRANCHISE_APP_PASSWORD=test-only-password",
        "JWT_SECRET=test-only-franchise-hub-secret-with-more-than-32-bytes",
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.mongo.MongoDataAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration"
})
class FranchiseHubApplicationTests {

    @MockitoBean
    private ReactiveBranchMongoRepository branchMongoRepository;

    @MockitoBean
    private ReactiveFranchiseMongoRepository franchiseMongoRepository;

    @MockitoBean
    private ReactiveProductMongoRepository productMongoRepository;

    @Test
    void contextLoads() {
    }
}
