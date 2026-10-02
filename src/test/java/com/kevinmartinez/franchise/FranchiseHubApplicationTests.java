package com.kevinmartinez.franchise;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.mongo.MongoDataAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.mongo.MongoReactiveRepositoriesAutoConfiguration"
})
class FranchiseHubApplicationTests {

    @Test
    void contextLoads() {
    }
}
