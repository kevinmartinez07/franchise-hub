package com.kevinmartinez.franchise.application.usecase.branch;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.kevinmartinez.franchise.application.exception.ResourceNotFoundException;
import com.kevinmartinez.franchise.application.repository.BranchRepository;
import com.kevinmartinez.franchise.application.repository.FranchiseRepository;
import com.kevinmartinez.franchise.application.usecase.branch.commands.AddBranchCommand;
import com.kevinmartinez.franchise.application.usecase.branch.commands.RenameBranchCommand;
import com.kevinmartinez.franchise.application.usecase.branch.commands.handler.AddBranchHandler;
import com.kevinmartinez.franchise.application.usecase.branch.commands.handler.RenameBranchHandler;
import com.kevinmartinez.franchise.application.usecase.branch.queries.GetBranchByIdQuery;
import com.kevinmartinez.franchise.application.usecase.branch.queries.GetBranchesQuery;
import com.kevinmartinez.franchise.application.usecase.branch.queries.handler.GetBranchByIdHandler;
import com.kevinmartinez.franchise.application.usecase.branch.queries.handler.GetBranchesHandler;
import com.kevinmartinez.franchise.domain.model.Branch;
import com.kevinmartinez.franchise.domain.model.Franchise;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class BranchHandlersTest {
    private static final Instant NOW = Instant.parse("2026-10-01T12:00:00Z");

    @Test
    void addBranchChecksFranchiseAndPersistsRelation() {
        FranchiseFake franchises = new FranchiseFake();
        BranchFake branches = new BranchFake();
        franchises.data.put("fr-1", Franchise.create("fr-1", "Acme", NOW));
        StepVerifier.create(new AddBranchHandler(franchises, branches)
                        .handle(new AddBranchCommand("fr-1", "  North  ")))
                .assertNext(branch -> { assertEquals("fr-1", branch.franchiseId()); assertEquals("North", branch.name()); })
                .verifyComplete();
        assertEquals(1, branches.saveCalls);
    }

    @Test
    void addBranchDoesNotPersistWhenFranchiseDoesNotExist() {
        BranchFake branches = new BranchFake();
        StepVerifier.create(new AddBranchHandler(new FranchiseFake(), branches)
                        .handle(new AddBranchCommand("missing", "North")))
                .expectError(ResourceNotFoundException.class).verify();
        assertEquals(0, branches.saveCalls);
    }

    @Test
    void renameBranchPersistsExistingBranch() {
        BranchFake branches = new BranchFake();
        branches.data.put("br-1", Branch.create("br-1", "fr-1", "North", NOW));
        StepVerifier.create(new RenameBranchHandler(branches)
                        .handle(new RenameBranchCommand("br-1", "Downtown")))
                .assertNext(branch -> assertEquals("Downtown", branch.name())).verifyComplete();
        assertEquals(1, branches.saveCalls);
    }

    @Test
    void renameMissingBranchReturnsNotFound() {
        StepVerifier.create(new RenameBranchHandler(new BranchFake())
                        .handle(new RenameBranchCommand("missing", "Downtown")))
                .expectError(ResourceNotFoundException.class).verify();
    }

    @Test
    void listBranchesWithoutFranchiseFilterReturnsAllBranches() {
        BranchFake branches = new BranchFake();
        branches.data.put("br-1", Branch.create("br-1", "fr-1", "North", NOW));
        branches.data.put("br-2", Branch.create("br-2", "fr-2", "South", NOW));
        StepVerifier.create(new GetBranchesHandler(branches, new FranchiseFake()).handle(new GetBranchesQuery(null)))
                .expectNextCount(2).verifyComplete();
    }

    @Test
    void listBranchesByFranchiseReturnsOnlyMatchingBranches() {
        FranchiseFake franchises = new FranchiseFake();
        BranchFake branches = new BranchFake();
        franchises.data.put("fr-1", Franchise.create("fr-1", "Acme", NOW));
        branches.data.put("br-1", Branch.create("br-1", "fr-1", "North", NOW));
        branches.data.put("br-2", Branch.create("br-2", "fr-2", "South", NOW));
        StepVerifier.create(new GetBranchesHandler(branches, franchises).handle(new GetBranchesQuery("fr-1")))
                .assertNext(branch -> assertEquals("br-1", branch.id())).verifyComplete();
    }

    @Test
    void listBranchesByMissingFranchiseReturnsNotFound() {
        StepVerifier.create(new GetBranchesHandler(new BranchFake(), new FranchiseFake())
                        .handle(new GetBranchesQuery("missing")))
                .expectError(ResourceNotFoundException.class).verify();
    }

    @Test
    void listBranchesByExistingFranchiseWithoutBranchesReturnsEmpty() {
        FranchiseFake franchises = new FranchiseFake();
        franchises.data.put("fr-1", Franchise.create("fr-1", "Acme", NOW));
        StepVerifier.create(new GetBranchesHandler(new BranchFake(), franchises).handle(new GetBranchesQuery("fr-1")))
                .verifyComplete();
    }

    @Test
    void getBranchByIdReturnsExistingBranch() {
        BranchFake branches = new BranchFake();
        branches.data.put("br-1", Branch.create("br-1", "fr-1", "North", NOW));
        StepVerifier.create(new GetBranchByIdHandler(branches).handle(new GetBranchByIdQuery("br-1")))
                .assertNext(branch -> assertEquals("North", branch.name())).verifyComplete();
    }

    @Test
    void getBranchByMissingIdReturnsNotFound() {
        StepVerifier.create(new GetBranchByIdHandler(new BranchFake())
                        .handle(new GetBranchByIdQuery("missing")))
                .expectError(ResourceNotFoundException.class).verify();
    }

    private static final class FranchiseFake implements FranchiseRepository {
        private final Map<String, Franchise> data = new LinkedHashMap<>();
        public Mono<Franchise> save(Franchise value) { data.put(value.getId(), value); return Mono.just(value); }
        public Flux<Franchise> findAll() { return Flux.fromIterable(data.values()); }
        public Mono<Franchise> findById(String id) { return Mono.justOrEmpty(data.get(id)); }
    }

    private static final class BranchFake implements BranchRepository {
        private final Map<String, Branch> data = new LinkedHashMap<>();
        private int saveCalls;
        public Mono<Branch> save(Branch value) { saveCalls++; data.put(value.getId(), value); return Mono.just(value); }
        public Flux<Branch> findAll() { return Flux.fromIterable(data.values()); }
        public Mono<Branch> findById(String id) { return Mono.justOrEmpty(data.get(id)); }
        public Flux<Branch> findByFranchiseId(String id) {
            return Flux.fromIterable(data.values()).filter(v -> v.getFranchiseId().equals(id));
        }
    }
}
