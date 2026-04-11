package com.onnick.reservationcamps.api.dto;

import com.onnick.reservationcamps.domain.UserRole;
import java.util.UUID;

public record UserResponse(UUID id, String email, UserRole role) {}

