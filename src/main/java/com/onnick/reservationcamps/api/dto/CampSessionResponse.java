package com.onnick.reservationcamps.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CampSessionResponse(UUID id, UUID campId, LocalDate startDate, LocalDate endDate, int capacity) {}

