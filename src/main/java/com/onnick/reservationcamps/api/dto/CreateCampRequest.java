package com.onnick.reservationcamps.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateCampRequest(@NotBlank String name, @Min(1) int basePriceCents) {}

