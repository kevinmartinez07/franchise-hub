package com.kevinmartinez.franchise.domain.model;

import java.time.Instant;

public final class Product {

    private final String id;
    private final String branchId;
    private String name;
    private int stock;
    private final Instant createdAt;
    private Instant updatedAt;

    private Product(String id, String branchId, String name, int stock, Instant createdAt, Instant updatedAt) {
        this.id = requireText(id, "El id");
        this.branchId = requireText(branchId, "El id de la sucursal");
        this.name = requireText(name, "El nombre");
        this.stock = requireStock(stock);
        this.createdAt = requireInstant(createdAt, "La fecha de creación");
        this.updatedAt = requireValidUpdatedAt(createdAt, updatedAt);
    }

    public static Product create(String id, String branchId, String name, int stock, Instant now) {
        return new Product(id, branchId, name, stock, now, now);
    }

    public static Product restore(
            String id,
            String branchId,
            String name,
            int stock,
            Instant createdAt,
            Instant updatedAt) {
        return new Product(id, branchId, name, stock, createdAt, updatedAt);
    }

    public void rename(String newName, Instant now) {
        String normalizedName = requireText(newName, "El nombre");
        Instant validNow = validateModificationTime(now);
        this.name = normalizedName;
        this.updatedAt = validNow;
    }

    public void updateStock(int newStock, Instant now) {
        int validStock = requireStock(newStock);
        Instant validNow = validateModificationTime(now);
        this.stock = validStock;
        this.updatedAt = validNow;
    }

    public String getId() {
        return id;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Product that)) {
            return false;
        }
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private Instant validateModificationTime(Instant now) {
        requireInstant(now, "La fecha de modificación");
        if (now.isBefore(updatedAt)) {
            throw new IllegalArgumentException(
                    "La fecha de modificación no puede ser anterior a la última actualización");
        }
        return now;
    }

    private static int requireStock(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        return value;
    }

    private static Instant requireValidUpdatedAt(Instant createdAt, Instant updatedAt) {
        requireInstant(createdAt, "La fecha de creación");
        requireInstant(updatedAt, "La fecha de actualización");
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException(
                    "La fecha de actualización no puede ser anterior a la fecha de creación");
        }
        return updatedAt;
    }

    private static String requireText(String value, String label) {
        if (value == null) {
            throw new IllegalArgumentException(label + " no puede ser nulo");
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(label + " no puede estar vacío");
        }
        return normalized;
    }

    private static Instant requireInstant(Instant value, String label) {
        if (value == null) {
            throw new IllegalArgumentException(label + " no puede ser nula");
        }
        return value;
    }
}
