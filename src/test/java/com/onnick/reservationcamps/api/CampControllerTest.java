package com.onnick.reservationcamps.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnick.reservationcamps.api.dto.CreateCampRequest;
import com.onnick.reservationcamps.api.dto.CreateSessionRequest;
import com.onnick.reservationcamps.domain.Camp;
import com.onnick.reservationcamps.domain.CampSession;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.service.Actor;
import com.onnick.reservationcamps.service.CampService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CampController.class)
@Import({ActorResolver.class, ApiExceptionHandler.class})
class CampControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CampService campService;

    @Test
    void createCampPassesAdminActor() throws Exception {
        var campId = UUID.randomUUID();
        when(campService.createCamp(any(), any(), anyInt()))
                .thenReturn(new Camp(campId, "Camp", 1000, Instant.EPOCH));

        mvc.perform(
                        post("/api/camps")
                                .header("X-Actor-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new CreateCampRequest("Camp", 1000))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(campId.toString()));

        var captor = ArgumentCaptor.forClass(Actor.class);
        verify(campService).createCamp(captor.capture(), any(), anyInt());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void createSessionWorks() throws Exception {
        var campId = UUID.randomUUID();
        var sessionId = UUID.randomUUID();

        when(campService.createSession(any(), any(), any(), any(), anyInt()))
                .thenReturn(
                        new CampSession(
                                sessionId,
                                campId,
                                LocalDate.of(2026, 5, 1),
                                LocalDate.of(2026, 5, 7),
                                10,
                                Instant.EPOCH));

        mvc.perform(
                        post("/api/camps/" + campId + "/sessions")
                                .header("X-Actor-Role", "ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new CreateSessionRequest(
                                                        LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 10))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sessionId.toString()));
    }
}
