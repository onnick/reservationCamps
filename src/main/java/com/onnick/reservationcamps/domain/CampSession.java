package com.onnick.reservationcamps.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "camp_session")
public class CampSession {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "camp_id", nullable = false)
    private Camp camp;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected CampSession() {}

    public CampSession(
            UUID id, Camp camp, LocalDate startDate, LocalDate endDate, int capacity, Instant createdAt) {
        this.id = id;
        this.camp = camp;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public Camp getCamp() {
        return camp;
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

