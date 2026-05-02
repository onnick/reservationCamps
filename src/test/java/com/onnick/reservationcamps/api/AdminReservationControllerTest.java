package com.onnick.reservationcamps.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onnick.reservationcamps.domain.AppUser;
import com.onnick.reservationcamps.domain.Reservation;
import com.onnick.reservationcamps.domain.ReservationStatus;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.service.ReservationService;
import com.onnick.reservationcamps.service.UserService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminReservationController.class)
@Import({ActorResolver.class, ApiExceptionHandler.class})
class AdminReservationControllerTest {
    @Autowired private MockMvc mvc;

    @MockBean private ReservationService reservationService;
    @MockBean private UserService userService;

    @Test
    void listAllRejectsWhenNotAdmin() throws Exception {
        mvc.perform(get("/api/admin/reservations").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void listAllReturnsArrayForAdmin() throws Exception {
        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();
        var reservationId = UUID.randomUUID();

        var reservation =
                new Reservation(
                        reservationId,
                        sessionId,
                        userId,
                        ReservationStatus.CREATED,
                        Instant.EPOCH,
                        null,
                        null,
                        null,
                        "Camp",
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 7));

        when(userService.requireAdmin(eq(userId))).thenReturn(new AppUser(userId, "admin@example.com", "hash", UserRole.ADMIN, Instant.EPOCH));
        when(reservationService.listAllReservations()).thenReturn(List.of(reservation));

        mvc.perform(get("/api/admin/reservations").header("X-Actor-Role", "ADMIN").header("X-Actor-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reservationId.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }
}
