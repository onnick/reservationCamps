package com.onnick.reservationcamps.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.domain.error.BusinessRuleViolationException;
import com.onnick.reservationcamps.domain.error.ForbiddenException;
import com.onnick.reservationcamps.domain.repo.CampRepository;
import com.onnick.reservationcamps.domain.repo.CampSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CampServiceTest {
    private static final Instant NOW = Instant.parse("2026-04-10T10:00:00Z");

    @Mock private CampRepository campRepository;
    @Mock private CampSessionRepository campSessionRepository;

    @Test
    void createCampRejectsWhenNotAdmin() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service = new CampService(campRepository, campSessionRepository, clock);

        assertThatThrownBy(() -> service.createCamp(new Actor(UUID.randomUUID(), UserRole.CUSTOMER), "Camp", 1000))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void createCampRejectsInvalidPrice() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service = new CampService(campRepository, campSessionRepository, clock);

        assertThatThrownBy(() -> service.createCamp(new Actor(UUID.randomUUID(), UserRole.ADMIN), "Camp", 0))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(
                        ex ->
                                assertThat(((BusinessRuleViolationException) ex).getCode())
                                        .isEqualTo("camp.base_price.invalid"));
    }

    @Test
    void createSessionRejectsWhenStartDateInPast() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service = new CampService(campRepository, campSessionRepository, clock);

        var campId = UUID.randomUUID();
        when(campRepository.existsById(campId)).thenReturn(true);

        assertThatThrownBy(
                        () ->
                                service.createSession(
                                        new Actor(UUID.randomUUID(), UserRole.ADMIN),
                                        campId,
                                        LocalDate.of(2026, 4, 9),
                                        LocalDate.of(2026, 4, 12),
                                        10))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(
                        ex ->
                                assertThat(((BusinessRuleViolationException) ex).getCode())
                                        .isEqualTo("session.start_in_past"));
    }

    @Test
    void createSessionRejectsWhenDatesInvalid() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service = new CampService(campRepository, campSessionRepository, clock);

        var campId = UUID.randomUUID();
        when(campRepository.existsById(campId)).thenReturn(true);

        assertThatThrownBy(
                        () ->
                                service.createSession(
                                        new Actor(UUID.randomUUID(), UserRole.ADMIN),
                                        campId,
                                        LocalDate.of(2026, 5, 7),
                                        LocalDate.of(2026, 5, 7),
                                        10))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(
                        ex ->
                                assertThat(((BusinessRuleViolationException) ex).getCode())
                                        .isEqualTo("session.date.invalid"));
    }
}
