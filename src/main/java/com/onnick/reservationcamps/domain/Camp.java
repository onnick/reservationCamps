package com.onnick.reservationcamps.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "camp")
public class Camp {
    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "base_price_cents", nullable = false)
    private int basePriceCents;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Camp() {}

    public Camp(UUID id, String name, int basePriceCents, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.basePriceCents = basePriceCents;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBasePriceCents() {
        return basePriceCents;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

