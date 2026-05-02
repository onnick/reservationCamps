package com.onnick.reservationcamps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.onnick.reservationcamps.domain.AppUser;
import com.onnick.reservationcamps.domain.Camp;
import com.onnick.reservationcamps.domain.CampSession;
import com.onnick.reservationcamps.domain.Reservation;
import com.onnick.reservationcamps.domain.ReservationStatus;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.domain.error.BusinessRuleViolationException;
import com.onnick.reservationcamps.domain.repo.AppUserRepository;
import com.onnick.reservationcamps.domain.repo.CampRepository;
import com.onnick.reservationcamps.domain.repo.CampSessionRepository;
import com.onnick.reservationcamps.domain.repo.ReservationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private static final Instant NOW = Instant.parse("2026-04-10T10:00:00Z");

    @Mock private AppUserRepository userRepository;
    @Mock private CampRepository campRepository;
    @Mock private CampSessionRepository sessionRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private NotificationPort notificationPort;

    @Test
    void createReservationRejectsDuplicate() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service =
                new ReservationService(
                        userRepository, campRepository, sessionRepository, reservationRepository, notificationPort, clock);

        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();

        var user = new AppUser(userId, "a@example.com", "hash", UserRole.CUSTOMER, NOW);
        var campId = UUID.randomUUID();
        var camp = new Camp(campId, "Camp", 1000, NOW);
        var session =
                new CampSession(
                        sessionId, campId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 10, NOW);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(campRepository.findById(campId)).thenReturn(Optional.of(camp));
        when(reservationRepository.findBySessionIdAndUserId(sessionId, userId))
                .thenReturn(
                        Optional.of(
                                new Reservation(
                                        UUID.randomUUID(),
                                        sessionId,
                                        userId,
                                        ReservationStatus.CREATED,
                                        NOW,
                                        null,
                                        null,
                                        null,
                                        "Camp",
                                        LocalDate.of(2026, 5, 1),
                                        LocalDate.of(2026, 5, 7))));

        assertThatThrownBy(() -> service.createReservation(sessionId, userId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(
                        ex ->
                                assertThat(((BusinessRuleViolationException) ex).getCode())
                                        .isEqualTo("reservation.duplicate"));
    }

    @Test
    void createReservationRejectsWhenSessionAlreadyStarted() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service =
                new ReservationService(
                        userRepository, campRepository, sessionRepository, reservationRepository, notificationPort, clock);

        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();

        var user = new AppUser(userId, "a@example.com", "hash", UserRole.CUSTOMER, NOW);
        var campId = UUID.randomUUID();
        var camp = new Camp(campId, "Camp", 1000, NOW);
        var session =
                new CampSession(
                        sessionId, campId, LocalDate.of(2026, 4, 10), LocalDate.of(2026, 4, 12), 10, NOW);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(campRepository.findById(campId)).thenReturn(Optional.of(camp));

        assertThatThrownBy(() -> service.createReservation(sessionId, userId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(
                        ex ->
                                assertThat(((BusinessRuleViolationException) ex).getCode())
                                        .isEqualTo("reservation.session_started"));
    }

    @Test
    void confirmSetsStatusAndSendsNotification() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service =
                new ReservationService(
                        userRepository, campRepository, sessionRepository, reservationRepository, notificationPort, clock);

        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();
        var reservationId = UUID.randomUUID();

        var campId = UUID.randomUUID();
        var camp = new Camp(campId, "Camp", 1000, NOW);
        var session =
                new CampSession(
                        sessionId, campId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 2, NOW);

        var reservation =
                new Reservation(
                        reservationId,
                        sessionId,
                        userId,
                        ReservationStatus.CREATED,
                        NOW,
                        null,
                        null,
                        null,
                        "Camp",
                        session.getStartDate(),
                        session.getEndDate());

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(reservationRepository.countBySessionIdAndStatus(sessionId, ReservationStatus.CONFIRMED)).thenReturn(1L);
        when(reservationRepository.countBySessionIdAndStatus(sessionId, ReservationStatus.PAID)).thenReturn(0L);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.confirmReservation(reservationId);

        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(saved.getConfirmedAt()).isEqualTo(NOW);
        verify(notificationPort).reservationConfirmed(reservationId);
    }

    @Test
    void confirmRejectsWhenStatusIsNotCreated() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service =
                new ReservationService(
                        userRepository, campRepository, sessionRepository, reservationRepository, notificationPort, clock);

        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();
        var reservationId = UUID.randomUUID();

        var campId = UUID.randomUUID();
        var session =
                new CampSession(
                        sessionId, campId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 2, NOW);

        var reservation =
                new Reservation(
                        reservationId,
                        sessionId,
                        userId,
                        ReservationStatus.CANCELLED,
                        NOW,
                        null,
                        null,
                        NOW,
                        "Camp",
                        session.getStartDate(),
                        session.getEndDate());

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> service.confirmReservation(reservationId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(
                        ex ->
                                assertThat(((BusinessRuleViolationException) ex).getCode())
                                        .isEqualTo("reservation.status.invalid"));
    }

    @Test
    void confirmRejectsWhenCapacityIsFull() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service =
                new ReservationService(
                        userRepository, campRepository, sessionRepository, reservationRepository, notificationPort, clock);

        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();
        var reservationId = UUID.randomUUID();

        var campId = UUID.randomUUID();
        var session =
                new CampSession(
                        sessionId, campId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 1, NOW);

        var reservation =
                new Reservation(
                        reservationId,
                        sessionId,
                        userId,
                        ReservationStatus.CREATED,
                        NOW,
                        null,
                        null,
                        null,
                        "Camp",
                        session.getStartDate(),
                        session.getEndDate());

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(reservationRepository.countBySessionIdAndStatus(sessionId, ReservationStatus.CONFIRMED)).thenReturn(1L);
        when(reservationRepository.countBySessionIdAndStatus(sessionId, ReservationStatus.PAID)).thenReturn(0L);

        assertThatThrownBy(() -> service.confirmReservation(reservationId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(
                        ex ->
                                assertThat(((BusinessRuleViolationException) ex).getCode())
                                        .isEqualTo("reservation.capacity_full"));

        verify(reservationRepository, never()).save(any());
        verify(notificationPort, never()).reservationConfirmed(any());
    }

    @Test
    void payRejectsWhenNotConfirmed() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service =
                new ReservationService(
                        userRepository, campRepository, sessionRepository, reservationRepository, notificationPort, clock);

        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();
        var reservationId = UUID.randomUUID();

        var reservation =
                new Reservation(
                        reservationId,
                        sessionId,
                        userId,
                        ReservationStatus.CREATED,
                        NOW,
                        null,
                        null,
                        null,
                        "Camp",
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 7));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> service.payReservation(reservationId))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(
                        ex ->
                                assertThat(((BusinessRuleViolationException) ex).getCode())
                                        .isEqualTo("reservation.status.invalid"));

        verify(notificationPort, never()).reservationPaid(any());
    }

    @Test
    void paySetsStatusAndSendsNotification() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service =
                new ReservationService(
                        userRepository, campRepository, sessionRepository, reservationRepository, notificationPort, clock);

        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();
        var reservationId = UUID.randomUUID();

        var reservation =
                new Reservation(
                        reservationId,
                        sessionId,
                        userId,
                        ReservationStatus.CONFIRMED,
                        NOW,
                        NOW,
                        null,
                        null,
                        "Camp",
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 7));

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.payReservation(reservationId);

        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PAID);
        assertThat(saved.getPaidAt()).isEqualTo(NOW);
        verify(notificationPort).reservationPaid(reservationId);
    }

    @Test
    void cancelSetsStatusAndTimestamp() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service =
                new ReservationService(
                        userRepository, campRepository, sessionRepository, reservationRepository, notificationPort, clock);

        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();
        var reservationId = UUID.randomUUID();

        var campId = UUID.randomUUID();
        var session =
                new CampSession(
                        sessionId, campId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 1, NOW);

        var reservation =
                new Reservation(
                        reservationId,
                        sessionId,
                        userId,
                        ReservationStatus.CREATED,
                        NOW,
                        null,
                        null,
                        null,
                        "Camp",
                        session.getStartDate(),
                        session.getEndDate());

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        var saved = service.cancelReservation(reservationId);

        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(saved.getCancelledAt()).isEqualTo(NOW);
    }
}
