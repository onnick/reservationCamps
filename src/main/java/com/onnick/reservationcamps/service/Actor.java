package com.onnick.reservationcamps.service;

import com.onnick.reservationcamps.domain.UserRole;
import java.util.UUID;

public record Actor(UUID userId, UserRole role) {}

