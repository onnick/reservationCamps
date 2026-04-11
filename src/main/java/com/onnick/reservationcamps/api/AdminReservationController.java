package com.onnick.reservationcamps.api;

import com.onnick.reservationcamps.api.dto.ReservationResponse;
import com.onnick.reservationcamps.domain.Reservation;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.domain.error.ForbiddenException;
import com.onnick.reservationcamps.service.ReservationService;
import com.onnick.reservationcamps.service.UserService;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminReservationController {
    private final ReservationService reservationService;
    private final ActorResolver actorResolver;
    private final UserService userService;

    public AdminReservationController(
            ReservationService reservationService, ActorResolver actorResolver, UserService userService) {
        this.reservationService = reservationService;
        this.actorResolver = actorResolver;
        this.userService = userService;
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> listAll(@RequestHeader HttpHeaders headers) {
        var actor = actorResolver.resolve(headers);
        if (actor == null || actor.role() != UserRole.ADMIN) {
            throw new ForbiddenException("Admin role required.");
        }
        userService.requireAdmin(actor.userId());
        return reservationService.listAllReservations().stream()
                .map(AdminReservationController::toResponse)
                .toList();
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
