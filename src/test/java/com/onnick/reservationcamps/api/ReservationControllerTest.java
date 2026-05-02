package com.onnick.reservationcamps.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnick.reservationcamps.api.dto.CreateReservationRequest;
import com.onnick.reservationcamps.domain.Reservation;
import com.onnick.reservationcamps.domain.ReservationStatus;
import com.onnick.reservationcamps.service.ReservationService;
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

@WebMvcTest(ReservationController.class)
@Import(ApiExceptionHandler.class)
class ReservationControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ReservationService reservationService;

    @Test
    void createReservationReturnsReservation() throws Exception {
        var sessionId = UUID.randomUUID();
        var userId = UUID.randomUUID();
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

        when(reservationService.createReservation(eq(sessionId), eq(userId))).thenReturn(reservation);

        mvc.perform(
                        post("/api/sessions/" + sessionId + "/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new CreateReservationRequest(userId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reservationId.toString()))
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void confirmCallsService() throws Exception {
        var reservationId = UUID.randomUUID();

        var reservation =
                new Reservation(
                        reservationId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ReservationStatus.CONFIRMED,
                        Instant.EPOCH,
                        Instant.EPOCH,
                        null,
                        null,
                        "Camp",
                        LocalDate.of(2026, 5, 1),
                        LocalDate.of(2026, 5, 7));

        when(reservationService.confirmReservation(eq(reservationId))).thenReturn(reservation);

        mvc.perform(post("/api/reservations/" + reservationId + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void listReservationsForUserReturnsArray() throws Exception {
        var userId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();

        var reservation =
                new Reservation(
                        UUID.randomUUID(),
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

        when(reservationService.listReservationsForUser(eq(userId))).thenReturn(List.of(reservation));

        mvc.perform(get("/api/users/" + userId + "/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reservation.getId().toString()))
                .andExpect(jsonPath("$[0].campName").value("Camp"))
                .andExpect(jsonPath("$[0].startDate").value("2026-05-01"))
                .andExpect(jsonPath("$[0].endDate").value("2026-05-07"))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }
}
