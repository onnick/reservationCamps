package com.onnick.reservationcamps.domain;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("app_user")
public class AppUser {
    @Id
    private UUID id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private UserRole role;

    private Instant createdAt;

    protected AppUser() {}

    public AppUser(UUID id, String email, String passwordHash, UserRole role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
