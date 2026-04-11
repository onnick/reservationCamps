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
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {
    private final AppUserRepository userRepository;
    private final Clock clock;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository, Clock clock, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clock = clock;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser createUser(String email, String password, UserRole role) {
        if (email == null || email.isBlank()) {
            throw new BusinessRuleViolationException("user.email.required", "Email is required.");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessRuleViolationException("user.password.required", "Password is required.");
        }
        if (role == null) {
            throw new BusinessRuleViolationException("user.role.required", "Role is required.");
        }

        var normalized = email.trim().toLowerCase();
        if (userRepository.findByEmail(normalized).isPresent()) {
            throw new BusinessRuleViolationException("user.email.duplicate", "Email already exists.");
        }

        var passwordHash = passwordEncoder.encode(password);
        var user = new AppUser(UUID.randomUUID(), normalized, passwordHash, role, Instant.now(clock));
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

    @Transactional(readOnly = true)
    public AppUser login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new BusinessRuleViolationException("user.email.required", "Email is required.");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessRuleViolationException("user.password.required", "Password is required.");
        }

        var normalized = email.trim().toLowerCase();
        var user =
                userRepository
                        .findByEmail(normalized)
                        .orElseThrow(() -> new com.onnick.reservationcamps.domain.error.NotFoundException("User not found."));

        var hash = user.getPasswordHash();
        if (hash == null || hash.isBlank() || !passwordEncoder.matches(password, hash)) {
            throw new com.onnick.reservationcamps.domain.error.ForbiddenException("Invalid credentials.");
        }
        return user;
    }
}
