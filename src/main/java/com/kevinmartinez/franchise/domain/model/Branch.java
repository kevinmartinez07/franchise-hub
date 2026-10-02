package com.kevinmartinez.franchise.domain.model;

import java.time.Instant;

public final class Branch {

    private final String id;
    private final String franchiseId;
    private String name;
    private final Instant createdAt;
    private Instant updatedAt;

    private Branch(String id, String franchiseId, String name, Instant createdAt, Instant updatedAt) {
        this.id = requireText(id, "El id");
        this.franchiseId = requireText(franchiseId, "El id de la franquicia");
        this.name = requireText(name, "El nombre");
        this.createdAt = requireInstant(createdAt, "La fecha de creación");
        this.updatedAt = requireValidUpdatedAt(createdAt, updatedAt);
    }

    public static Branch create(String id, String franchiseId, String name, Instant now) {
        return new Branch(id, franchiseId, name, now, now);
    }

    public static Branch restore(
            String id,
            String franchiseId,
            String name,
            Instant createdAt,
            Instant updatedAt) {
        return new Branch(id, franchiseId, name, createdAt, updatedAt);
    }

    public void rename(String newName, Instant now) {
        String normalizedName = requireText(newName, "El nombre");
        Instant validNow = validateModificationTime(now);
        this.name = normalizedName;
        this.updatedAt = validNow;
    }

    public String getId() {
        return id;
    }

    public String getFranchiseId() {
        return franchiseId;
    }

    public String getName() {
        return name;
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
        if (!(other instanceof Branch that)) {
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
