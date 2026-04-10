package com.onnick.reservationcamps.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "name",
                "reservationCamps",
                "health",
                "/actuator/health",
                "docs",
                Map.of(
                        "createUser",
                        "POST /api/users",
                        "createCamp",
                        "POST /api/camps (X-Actor-Role: ADMIN)",
                        "createSession",
                        "POST /api/camps/{campId}/sessions (X-Actor-Role: ADMIN)",
                        "createReservation",
                        "POST /api/sessions/{sessionId}/reservations",
                        "confirmReservation",
                        "POST /api/reservations/{reservationId}/confirm",
                        "payReservation",
                        "POST /api/reservations/{reservationId}/pay",
                        "cancelReservation",
                        "POST /api/reservations/{reservationId}/cancel"));
    }
}

