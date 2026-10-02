package com.kevinmartinez.franchise.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class FranchiseTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-01T05:00:00Z");
    private static final Instant RENAMED_AT = Instant.parse("2026-10-01T05:05:00Z");

    @Test
    void createNormalizesNameAndUsesCreationTimeForBothTimestamps() {
        Franchise franchise = Franchise.create("fr-1", "  Acme  ", CREATED_AT);

        assertAll(
                () -> assertEquals("fr-1", franchise.getId()),
                () -> assertEquals("Acme", franchise.getName()),
                () -> assertEquals(CREATED_AT, franchise.getCreatedAt()),
                () -> assertEquals(CREATED_AT, franchise.getUpdatedAt()));
    }

    @Test
    void createRejectsInvalidIdentityAndName() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Franchise.create(null, "Acme", CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Franchise.create(" ", "Acme", CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Franchise.create("fr-1", null, CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Franchise.create("fr-1", "  ", CREATED_AT)));
    }

    @Test
    void renameChangesNameAndUpdatedAt() {
        Franchise franchise = Franchise.create("fr-1", "Acme", CREATED_AT);

        franchise.rename("  Acme Colombia  ", RENAMED_AT);

        assertEquals("Acme Colombia", franchise.getName());
        assertEquals(RENAMED_AT, franchise.getUpdatedAt());
    }

    @Test
    void renameRejectsInvalidNameAndBackwardsTime() {
        Franchise franchise = Franchise.create("fr-1", "Acme", CREATED_AT);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> franchise.rename(" ", RENAMED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> franchise.rename(null, RENAMED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> franchise.rename("New name", CREATED_AT.minusSeconds(1))));
    }

    @Test
    void restorePreservesStateAndRejectsInvalidTimeRange() {
        Instant updatedAt = RENAMED_AT;
        Franchise restored = Franchise.restore("fr-1", "Acme", CREATED_AT, updatedAt);

        assertAll(
                () -> assertEquals("fr-1", restored.getId()),
                () -> assertEquals("Acme", restored.getName()),
                () -> assertEquals(CREATED_AT, restored.getCreatedAt()),
                () -> assertEquals(updatedAt, restored.getUpdatedAt()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Franchise.restore("fr-1", "Acme", updatedAt, CREATED_AT)));
    }

    @Test
    void equalityUsesOnlyIdentity() {
        Franchise first = Franchise.create("fr-1", "Acme", CREATED_AT);
        Franchise sameId = Franchise.restore("fr-1", "Other", CREATED_AT, RENAMED_AT);
        Franchise differentId = Franchise.create("fr-2", "Acme", CREATED_AT);

        assertAll(
                () -> assertEquals(first, sameId),
                () -> assertEquals(first.hashCode(), sameId.hashCode()),
                () -> assertNotEquals(first, differentId));
    }
}
