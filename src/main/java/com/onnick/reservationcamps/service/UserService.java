package com.onnick.reservationcamps.service;

import com.onnick.reservationcamps.domain.AppUser;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.domain.error.BusinessRuleViolationException;
import com.onnick.reservationcamps.domain.repo.AppUserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final AppUserRepository userRepository;
    private final Clock clock;

    public UserService(AppUserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public AppUser createUser(String email, UserRole role) {
        if (email == null || email.isBlank()) {
            throw new BusinessRuleViolationException("user.email.required", "Email is required.");
        }
        if (role == null) {
            throw new BusinessRuleViolationException("user.role.required", "Role is required.");
        }

        var normalized = email.trim().toLowerCase();
        if (userRepository.findByEmail(normalized).isPresent()) {
            throw new BusinessRuleViolationException("user.email.duplicate", "Email already exists.");
        }

        var user = new AppUser(UUID.randomUUID(), normalized, role, Instant.now(clock));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        var normalized = email.trim().toLowerCase();
        if (normalized.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(normalized);
    }
}
