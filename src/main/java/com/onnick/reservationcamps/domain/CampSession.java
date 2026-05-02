package com.onnick.reservationcamps.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("camp_session")
public class CampSession {
    @Id
    private UUID id;

    private UUID campId;

    private LocalDate startDate;

    private LocalDate endDate;

    private int capacity;

    private Instant createdAt;

    protected CampSession() {}

    public CampSession(
            UUID id, UUID campId, LocalDate startDate, LocalDate endDate, int capacity, Instant createdAt) {
        this.id = id;
        this.campId = campId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCampId() {
        return campId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
