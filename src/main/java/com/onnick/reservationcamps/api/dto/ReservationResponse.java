package com.onnick.reservationcamps.api.dto;

import com.onnick.reservationcamps.domain.ReservationStatus;
import java.time.Instant;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID sessionId,
        UUID userId,
        ReservationStatus status,
        Instant createdAt,
        Instant confirmedAt,
        Instant paidAt,
        Instant cancelledAt) {}

