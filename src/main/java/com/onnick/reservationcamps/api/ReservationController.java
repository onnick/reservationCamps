package com.onnick.reservationcamps.api;

import com.onnick.reservationcamps.api.dto.CreateReservationRequest;
import com.onnick.reservationcamps.api.dto.ReservationResponse;
import com.onnick.reservationcamps.api.dto.UserReservationRowResponse;
import com.onnick.reservationcamps.domain.Reservation;
import com.onnick.reservationcamps.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations/{reservationId}")
    public ReservationResponse get(@PathVariable UUID reservationId) {
        return toResponse(reservationService.getReservation(reservationId));
    }

    @GetMapping("/users/{userId}/reservations")
    public List<UserReservationRowResponse> listForUser(@PathVariable UUID userId) {
        return reservationService.listReservationsForUser(userId).stream()
                .map(r -> new UserReservationRowResponse(
                        r.getId(),
                        r.getSession().getCamp().getName(),
                        r.getSession().getStartDate(),
                        r.getSession().getEndDate(),
                        r.getStatus()))
                .toList();
    }

    @PostMapping("/sessions/{sessionId}/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
            @PathVariable UUID sessionId, @Valid @RequestBody CreateReservationRequest request) {
        var reservation = reservationService.createReservation(sessionId, request.userId());
        return toResponse(reservation);
    }

    @PostMapping("/reservations/{reservationId}/confirm")
    public ReservationResponse confirm(@PathVariable UUID reservationId) {
        return toResponse(reservationService.confirmReservation(reservationId));
    }

    @PostMapping("/reservations/{reservationId}/pay")
    public ReservationResponse pay(@PathVariable UUID reservationId) {
        return toResponse(reservationService.payReservation(reservationId));
    }

    @PostMapping("/reservations/{reservationId}/cancel")
    public ReservationResponse cancel(@PathVariable UUID reservationId) {
        return toResponse(reservationService.cancelReservation(reservationId));
    }

    private static ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getSession().getId(),
                reservation.getUser().getId(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getConfirmedAt(),
                reservation.getPaidAt(),
                reservation.getCancelledAt());
    }
}
