package com.kevinmartinez.franchise.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ProductTest {

    private static final Instant CREATED_AT = Instant.parse("2026-10-01T05:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-10-01T05:05:00Z");

    @Test
    void createNormalizesNameAndSetsTimestamps() {
        Product product = Product.create("pr-1", "br-1", "  Coffee  ", 10, CREATED_AT);

        assertAll(
                () -> assertEquals("pr-1", product.getId()),
                () -> assertEquals("br-1", product.getBranchId()),
                () -> assertEquals("Coffee", product.getName()),
                () -> assertEquals(10, product.getStock()),
                () -> assertEquals(CREATED_AT, product.getCreatedAt()),
                () -> assertEquals(CREATED_AT, product.getUpdatedAt()));
    }

    @Test
    void createRejectsInvalidFields() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Product.create(null, "br-1", "Coffee", 10, CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Product.create(" ", "br-1", "Coffee", 10, CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Product.create("pr-1", null, "Coffee", 10, CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Product.create("pr-1", " ", "Coffee", 10, CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Product.create("pr-1", "br-1", null, 10, CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Product.create("pr-1", "br-1", " ", 10, CREATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Product.create("pr-1", "br-1", "Coffee", -1, CREATED_AT)));
    }

    @Test
    void updateStockChangesStockAndUpdatedAt() {
        Product product = Product.create("pr-1", "br-1", "Coffee", 10, CREATED_AT);

        product.updateStock(25, UPDATED_AT);

        assertEquals(25, product.getStock());
        assertEquals(UPDATED_AT, product.getUpdatedAt());
    }

    @Test
    void updateStockRejectsNegativeAndBackwardsValues() {
        Product product = Product.create("pr-1", "br-1", "Coffee", 10, CREATED_AT);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> product.updateStock(-1, UPDATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> product.updateStock(20, CREATED_AT.minusSeconds(1))));
    }

    @Test
    void renameChangesNameAndUpdatedAt() {
        Product product = Product.create("pr-1", "br-1", "Coffee", 10, CREATED_AT);

        product.rename("  Espresso  ", UPDATED_AT);

        assertEquals("Espresso", product.getName());
        assertEquals(UPDATED_AT, product.getUpdatedAt());
    }

    @Test
    void renameRejectsInvalidName() {
        Product product = Product.create("pr-1", "br-1", "Coffee", 10, CREATED_AT);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> product.rename(null, UPDATED_AT)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> product.rename(" ", UPDATED_AT)));
    }

    @Test
    void restorePreservesStateAndRejectsInvalidTimeRange() {
        Product restored = Product.restore("pr-1", "br-1", "Coffee", 10, CREATED_AT, UPDATED_AT);

        assertAll(
                () -> assertEquals("pr-1", restored.getId()),
                () -> assertEquals("br-1", restored.getBranchId()),
                () -> assertEquals("Coffee", restored.getName()),
                () -> assertEquals(10, restored.getStock()),
                () -> assertEquals(CREATED_AT, restored.getCreatedAt()),
                () -> assertEquals(UPDATED_AT, restored.getUpdatedAt()),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> Product.restore("pr-1", "br-1", "Coffee", 10, UPDATED_AT, CREATED_AT)));
    }

    @Test
    void equalityUsesOnlyIdentity() {
        Product first = Product.create("pr-1", "br-1", "Coffee", 10, CREATED_AT);
        Product sameId = Product.restore("pr-1", "br-1", "Other", 99, CREATED_AT, UPDATED_AT);
        Product differentId = Product.create("pr-2", "br-1", "Coffee", 10, CREATED_AT);

        assertAll(
                () -> assertEquals(first, sameId),
                () -> assertEquals(first.hashCode(), sameId.hashCode()),
                () -> assertNotEquals(first, differentId));
    }
}
