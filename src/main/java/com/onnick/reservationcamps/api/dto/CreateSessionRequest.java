package com.onnick.reservationcamps.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateSessionRequest(@NotNull LocalDate startDate, @NotNull LocalDate endDate, @Min(1) int capacity) {}

