package com.onnick.reservationcamps.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("reservation")
public class Reservation {
    @Id
    private UUID id;

    private UUID sessionId;

    private UUID userId;

    private ReservationStatus status;

    private Instant createdAt;

    private Instant confirmedAt;

    private Instant paidAt;

    private Instant cancelledAt;

    // Denormalized snapshot used by the login UI list endpoint (avoid server-side "joins" in Mongo).
    private String campName;
    private LocalDate startDate;
    private LocalDate endDate;

    protected Reservation() {}

    public Reservation(
            UUID id,
            UUID sessionId,
            UUID userId,
            ReservationStatus status,
            Instant createdAt,
            Instant confirmedAt,
            Instant paidAt,
            Instant cancelledAt,
            String campName,
            LocalDate startDate,
            LocalDate endDate) {
        this.id = id;
        this.sessionId = sessionId;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
        this.paidAt = paidAt;
        this.cancelledAt = cancelledAt;
        this.campName = campName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public String getCampName() {
        return campName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
