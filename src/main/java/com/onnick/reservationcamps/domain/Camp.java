package com.onnick.reservationcamps.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("camp")
public class Camp {
    @Id
    private UUID id;

    private String name;

    private int basePriceCents;

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
