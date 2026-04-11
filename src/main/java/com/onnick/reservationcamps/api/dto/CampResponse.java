package com.onnick.reservationcamps.api.dto;

import java.util.UUID;

public record CampResponse(UUID id, String name, int basePriceCents) {}

