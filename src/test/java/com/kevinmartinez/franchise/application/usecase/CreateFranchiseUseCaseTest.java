package com.kevinmartinez.franchise.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.kevinmartinez.franchise.application.port.FranchiseRepository;
import com.kevinmartinez.franchise.domain.model.Franchise;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class CreateFranchiseUseCaseTest {

    @Test
    void createsPersistsAndReturnsNormalizedFranchise() {
        FakeFranchiseRepository repository = new FakeFranchiseRepository();
        CreateFranchiseUseCase useCase = new CreateFranchiseUseCase(repository);

        StepVerifier.create(useCase.execute("  Acme  "))
                .assertNext(franchise -> {
                    assertEquals("Acme", franchise.getName());
                    assertNotNull(franchise.getCreatedAt());
                    assertNotNull(franchise.getUpdatedAt());
                    assertEquals(franchise.getCreatedAt(), franchise.getUpdatedAt());
                    assertEquals(franchise, repository.saved);
                })
                .verifyComplete();

        assertEquals(1, repository.saveCalls);
    }

    @Test
    void generatesAnIndependentId() {
        FakeFranchiseRepository repository = new FakeFranchiseRepository();
        CreateFranchiseUseCase useCase = new CreateFranchiseUseCase(repository);

        StepVerifier.create(useCase.execute("Acme"))
                .assertNext(franchise -> assertFalse(franchise.getId().isBlank()))
                .verifyComplete();
    }

    @Test
    void invalidNameFailsReactivelyAndDoesNotInvokeRepository() {
        FakeFranchiseRepository repository = new FakeFranchiseRepository();
        CreateFranchiseUseCase useCase = new CreateFranchiseUseCase(repository);

        StepVerifier.create(useCase.execute("  "))
                .expectError(IllegalArgumentException.class)
                .verify();

        assertEquals(0, repository.saveCalls);
    }

    @Test
    void nullNameFailsReactivelyAndDoesNotInvokeRepository() {
        FakeFranchiseRepository repository = new FakeFranchiseRepository();
        CreateFranchiseUseCase useCase = new CreateFranchiseUseCase(repository);

        StepVerifier.create(useCase.execute(null))
                .expectError(IllegalArgumentException.class)
                .verify();

        assertEquals(0, repository.saveCalls);
    }

    private static final class FakeFranchiseRepository implements FranchiseRepository {

        private Franchise saved;
        private int saveCalls;

        @Override
        public Mono<Franchise> save(Franchise franchise) {
            saved = franchise;
            saveCalls++;
            return Mono.just(franchise);
        }
    }
}
