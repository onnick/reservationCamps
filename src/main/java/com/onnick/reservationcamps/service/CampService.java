package com.onnick.reservationcamps.service;

import com.onnick.reservationcamps.domain.Camp;
import com.onnick.reservationcamps.domain.CampSession;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.domain.error.BusinessRuleViolationException;
import com.onnick.reservationcamps.domain.error.ForbiddenException;
import com.onnick.reservationcamps.domain.error.NotFoundException;
import com.onnick.reservationcamps.domain.repo.CampRepository;
import com.onnick.reservationcamps.domain.repo.CampSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CampService {
    private final CampRepository campRepository;
    private final CampSessionRepository campSessionRepository;
    private final Clock clock;

    public CampService(CampRepository campRepository, CampSessionRepository campSessionRepository, Clock clock) {
        this.campRepository = campRepository;
        this.campSessionRepository = campSessionRepository;
        this.clock = clock;
    }

    public Camp createCamp(Actor actor, String name, int basePriceCents) {
        requireAdmin(actor);

        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("camp.name.required", "Camp name is required.");
        }
        if (basePriceCents <= 0) {
            throw new BusinessRuleViolationException(
                    "camp.base_price.invalid", "Base price must be a positive integer (cents).");
        }

        var camp = new Camp(UUID.randomUUID(), name.trim(), basePriceCents, Instant.now(clock));
        return campRepository.save(camp);
    }

    public CampSession createSession(Actor actor, UUID campId, LocalDate startDate, LocalDate endDate, int capacity) {
        requireAdmin(actor);

        if (!campRepository.existsById(campId)) {
            throw new NotFoundException("Camp not found: " + campId);
        }

        if (startDate == null || endDate == null) {
            throw new BusinessRuleViolationException("session.date.required", "Start and end date are required.");
        }
        if (!startDate.isBefore(endDate)) {
            throw new BusinessRuleViolationException("session.date.invalid", "Start date must be before end date.");
        }
        if (capacity <= 0) {
            throw new BusinessRuleViolationException("session.capacity.invalid", "Capacity must be a positive integer.");
        }

        var today = LocalDate.now(clock);
        if (startDate.isBefore(today)) {
            throw new BusinessRuleViolationException(
                    "session.start_in_past", "Session start date cannot be in the past.");
        }

        var session =
                new CampSession(UUID.randomUUID(), campId, startDate, endDate, capacity, Instant.now(clock));
        return campSessionRepository.save(session);
    }

    public List<Camp> listCamps() {
        return campRepository.findAll();
    }

    public List<CampSession> listSessions(UUID campId) {
        if (!campRepository.existsById(campId)) {
            throw new NotFoundException("Camp not found: " + campId);
        }
        return campSessionRepository.findAllByCampIdOrderByStartDateAsc(campId);
    }

    private static void requireAdmin(Actor actor) {
        if (actor == null || actor.role() != UserRole.ADMIN) {
            throw new ForbiddenException("Admin role required.");
        }
    }
}
