package com.onnick.reservationcamps.api.dto;

import com.onnick.reservationcamps.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(@NotBlank @Email String email, UserRole role) {}

