package com.onnick.reservationcamps.service;

import com.onnick.reservationcamps.domain.Reservation;
import com.onnick.reservationcamps.domain.ReservationStatus;
import com.onnick.reservationcamps.domain.error.BusinessRuleViolationException;
import com.onnick.reservationcamps.domain.error.NotFoundException;
import com.onnick.reservationcamps.domain.repo.AppUserRepository;
import com.onnick.reservationcamps.domain.repo.CampRepository;
import com.onnick.reservationcamps.domain.repo.CampSessionRepository;
import com.onnick.reservationcamps.domain.repo.ReservationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {
    private final AppUserRepository userRepository;
    private final CampRepository campRepository;
    private final CampSessionRepository sessionRepository;
    private final ReservationRepository reservationRepository;
    private final NotificationPort notificationPort;
    private final Clock clock;

    public ReservationService(
            AppUserRepository userRepository,
            CampRepository campRepository,
            CampSessionRepository sessionRepository,
            ReservationRepository reservationRepository,
            NotificationPort notificationPort,
            Clock clock) {
        this.userRepository = userRepository;
        this.campRepository = campRepository;
        this.sessionRepository = sessionRepository;
        this.reservationRepository = reservationRepository;
        this.notificationPort = notificationPort;
        this.clock = clock;
    }

    public Reservation createReservation(UUID sessionId, UUID userId) {
        var session =
                sessionRepository
                        .findById(sessionId)
                        .orElseThrow(() -> new NotFoundException("Session not found: " + sessionId));
        var user =
                userRepository
                        .findById(userId)
                        .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        var camp =
                campRepository
                        .findById(session.getCampId())
                        .orElseThrow(() -> new NotFoundException("Camp not found: " + session.getCampId()));

        var today = LocalDate.now(clock);
        if (!today.isBefore(session.getStartDate())) {
            throw new BusinessRuleViolationException(
                    "reservation.session_started", "Cannot reserve a session that already started.");
        }

        var existing = reservationRepository.findBySessionIdAndUserId(sessionId, userId);
        if (existing.isPresent()) {
            throw new BusinessRuleViolationException(
                    "reservation.duplicate", "Reservation for this user and session already exists.");
        }

        var reservation =
                new Reservation(
                        UUID.randomUUID(),
                        session.getId(),
                        user.getId(),
                        ReservationStatus.CREATED,
                        Instant.now(clock),
                        null,
                        null,
                        null,
                        camp.getName(),
                        session.getStartDate(),
                        session.getEndDate());
        return reservationRepository.save(reservation);
    }

    public Reservation confirmReservation(UUID reservationId) {
        var reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));

        var session =
                sessionRepository
                        .findById(reservation.getSessionId())
                        .orElseThrow(
                                () -> new NotFoundException("Session not found: " + reservation.getSessionId()));

        var today = LocalDate.now(clock);
        if (!today.isBefore(session.getStartDate())) {
            throw new BusinessRuleViolationException(
                    "reservation.session_started", "Cannot confirm a reservation for a session that already started.");
        }

        if (reservation.getStatus() != ReservationStatus.CREATED) {
            throw new BusinessRuleViolationException(
                    "reservation.status.invalid",
                    "Only a CREATED reservation can be confirmed. Current: " + reservation.getStatus());
        }

        var taken = confirmedOrPaidCount(session.getId());
        if (taken >= session.getCapacity()) {
            throw new BusinessRuleViolationException(
                    "reservation.capacity_full", "Cannot confirm reservation. Capacity is full.");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setConfirmedAt(Instant.now(clock));
        var saved = reservationRepository.save(reservation);
        notificationPort.reservationConfirmed(saved.getId());
        return saved;
    }

    public Reservation payReservation(UUID reservationId) {
        var reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleViolationException(
                    "reservation.status.invalid",
                    "Only a CONFIRMED reservation can be paid. Current: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.PAID);
        reservation.setPaidAt(Instant.now(clock));
        var saved = reservationRepository.save(reservation);
        notificationPort.reservationPaid(saved.getId());
        return saved;
    }

    public Reservation cancelReservation(UUID reservationId) {
        var reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));

        var session =
                sessionRepository
                        .findById(reservation.getSessionId())
                        .orElseThrow(
                                () -> new NotFoundException("Session not found: " + reservation.getSessionId()));

        var today = LocalDate.now(clock);
        if (!today.isBefore(session.getStartDate())) {
            throw new BusinessRuleViolationException(
                    "reservation.session_started", "Cannot cancel reservation for a session that already started.");
        }

        if (reservation.getStatus() == ReservationStatus.PAID) {
            throw new BusinessRuleViolationException(
                    "reservation.status.invalid", "A PAID reservation cannot be cancelled.");
        }
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessRuleViolationException(
                    "reservation.status.invalid", "Reservation is already cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(Instant.now(clock));
        return reservationRepository.save(reservation);
    }

    public Reservation getReservation(UUID reservationId) {
        return reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found: " + reservationId));
    }

    public List<Reservation> listReservationsForUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found: " + userId);
        }
        return reservationRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Reservation> listAllReservations() {
        return reservationRepository.findAllByOrderByCreatedAtDesc();
    }

    private long confirmedOrPaidCount(UUID sessionId) {
        var confirmed = reservationRepository.countBySessionIdAndStatus(sessionId, ReservationStatus.CONFIRMED);
        var paid = reservationRepository.countBySessionIdAndStatus(sessionId, ReservationStatus.PAID);
        return confirmed + paid;
    }
}
