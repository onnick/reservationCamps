package com.onnick.reservationcamps.service;

import com.onnick.reservationcamps.domain.AppUser;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.domain.error.BusinessRuleViolationException;
import com.onnick.reservationcamps.domain.error.ForbiddenException;
import com.onnick.reservationcamps.domain.error.NotFoundException;
import com.onnick.reservationcamps.domain.repo.AppUserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public List<AppUser> searchByEmail(String q) {
        if (q == null) {
            return List.of();
        }
        var normalized = q.trim().toLowerCase();
        if (normalized.isBlank()) {
            return List.of();
        }
        // Used by the demo UI for "pick existing user". In a real app this should be access-controlled.
        return userRepository.findTop20ByEmailContainingIgnoreCaseOrderByEmailAsc(normalized);
    }

    @Transactional(readOnly = true)
    public List<AppUser> recentUsers() {
        // Used by the demo UI for "pick existing user". In a real app this should be access-controlled.
        return userRepository.findTop20ByOrderByEmailAsc();
    }

    @Transactional(readOnly = true)
    public List<AppUser> listUsers(int limit) {
        int capped = Math.min(Math.max(limit, 1), 500);
        return userRepository
                .findAll(PageRequest.of(0, capped, Sort.by(Sort.Direction.ASC, "email")))
                .getContent();
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
        var user = userRepository.findByEmail(normalized).orElseThrow(() -> new NotFoundException("User not found."));

        var hash = user.getPasswordHash();
        if (hash == null || hash.isBlank() || !passwordEncoder.matches(password, hash)) {
            throw new ForbiddenException("Invalid credentials.");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public AppUser requireAdmin(UUID userId) {
        if (userId == null) {
            throw new ForbiddenException("Admin role required.");
        }
        var user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found."));
        if (user.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Admin role required.");
        }
        return user;
    }
}
