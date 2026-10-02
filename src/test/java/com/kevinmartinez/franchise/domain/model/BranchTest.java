package com.kevinmartinez.franchise.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class BranchTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-01T05:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-10-01T05:05:00Z");

    @Test
    void createNormalizesNameAndSetsTimestamps() {
        Branch branch = Branch.create("br-1", "fr-1", "  North  ", CREATED_AT);

        assertAll(
                () -> assertEquals("br-1", branch.getId()),
                () -> assertEquals("fr-1", branch.getFranchiseId()),
                () -> assertEquals("North", branch.getName()),
                () -> assertEquals(CREATED_AT, branch.getCreatedAt()),
                () -> assertEquals(CREATED_AT, branch.getUpdatedAt()));
    }

    @Test
    void createRejectsInvalidIdentityAndName() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Branch.create(null, "fr-1", "North", CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Branch.create(" ", "fr-1", "North", CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Branch.create("br-1", null, "North", CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Branch.create("br-1", " ", "North", CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Branch.create("br-1", "fr-1", null, CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Branch.create("br-1", "fr-1", " ", CREATED_AT)));
    }

    @Test
    void renameChangesNameAndUpdatedAt() {
        Branch branch = Branch.create("br-1", "fr-1", "North", CREATED_AT);

        branch.rename("  Downtown  ", UPDATED_AT);

        assertEquals("Downtown", branch.getName());
        assertEquals(UPDATED_AT, branch.getUpdatedAt());
    }

    @Test
    void renameRejectsInvalidName() {
        Branch branch = Branch.create("br-1", "fr-1", "North", CREATED_AT);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> branch.rename(null, UPDATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> branch.rename(" ", UPDATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> branch.rename("South", CREATED_AT.minusSeconds(1))));
    }

    @Test
    void restorePreservesStateAndTimestamps() {
        Branch restored = Branch.restore("br-1", "fr-1", "North", CREATED_AT, UPDATED_AT);

        assertAll(
                () -> assertEquals("br-1", restored.getId()),
                () -> assertEquals("fr-1", restored.getFranchiseId()),
                () -> assertEquals("North", restored.getName()),
                () -> assertEquals(CREATED_AT, restored.getCreatedAt()),
                () -> assertEquals(UPDATED_AT, restored.getUpdatedAt()));
    }

    @Test
    void equalityUsesOnlyIdentity() {
        Branch first = Branch.create("br-1", "fr-1", "North", CREATED_AT);
        Branch sameId = Branch.restore("br-1", "fr-1", "Other", CREATED_AT, UPDATED_AT);
        Branch differentId = Branch.create("br-2", "fr-1", "North", CREATED_AT);

        assertAll(
                () -> assertEquals(first, sameId),
                () -> assertEquals(first.hashCode(), sameId.hashCode()),
                () -> assertNotEquals(first, differentId));
    }
}
