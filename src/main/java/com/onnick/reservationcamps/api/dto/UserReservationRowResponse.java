package com.onnick.reservationcamps.api.dto;

import com.onnick.reservationcamps.domain.ReservationStatus;
import java.time.LocalDate;
import java.util.UUID;

public record UserReservationRowResponse(
        UUID id, String campName, LocalDate startDate, LocalDate endDate, ReservationStatus status) {}
