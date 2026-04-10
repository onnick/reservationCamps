package com.onnick.reservationcamps.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateReservationRequest(@NotNull UUID userId) {}

