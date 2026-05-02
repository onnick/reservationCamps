package com.onnick.reservationcamps.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onnick.reservationcamps.api.dto.CreateUserRequest;
import com.onnick.reservationcamps.api.dto.LoginRequest;
import com.onnick.reservationcamps.domain.AppUser;
import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.service.UserService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(ApiExceptionHandler.class)
class UserControllerTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserService userService;

    @Test
    void createsUser() throws Exception {
        var id = UUID.randomUUID();
        when(userService.createUser(eq("a@example.com"), eq("password123"), eq(UserRole.CUSTOMER)))
                .thenReturn(new AppUser(id, "a@example.com", "hash", UserRole.CUSTOMER, Instant.EPOCH));

        mvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new CreateUserRequest("a@example.com", "password123", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void rejectsInvalidBody() throws Exception {
        when(userService.createUser(any(), any(), any())).thenThrow(new AssertionError("should not be called"));

        mvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsId() throws Exception {
        var id = UUID.randomUUID();
        when(userService.login(eq("a@example.com"), eq("password123")))
                .thenReturn(new AppUser(id, "a@example.com", "hash", UserRole.CUSTOMER, Instant.EPOCH));

        mvc.perform(
                        post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new LoginRequest("a@example.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void searchReturnsUsers() throws Exception {
        var u1 = new AppUser(UUID.randomUUID(), "a@example.com", "hash", UserRole.CUSTOMER, Instant.EPOCH);
        var u2 = new AppUser(UUID.randomUUID(), "b@example.com", "hash", UserRole.CUSTOMER, Instant.EPOCH);
        when(userService.searchByEmail(eq("ex"))).thenReturn(List.of(u1, u2));

        mvc.perform(get("/api/users/search?q=ex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(u1.getId().toString()))
                .andExpect(jsonPath("$[0].email").value("a@example.com"))
                .andExpect(jsonPath("$[1].id").value(u2.getId().toString()))
                .andExpect(jsonPath("$[1].email").value("b@example.com"));
    }

    @Test
    void recentReturnsUsers() throws Exception {
        var u1 = new AppUser(UUID.randomUUID(), "a@example.com", "hash", UserRole.CUSTOMER, Instant.EPOCH);
        var u2 = new AppUser(UUID.randomUUID(), "b@example.com", "hash", UserRole.CUSTOMER, Instant.EPOCH);
        when(userService.recentUsers()).thenReturn(List.of(u1, u2));

        mvc.perform(get("/api/users/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(u1.getId().toString()))
                .andExpect(jsonPath("$[0].email").value("a@example.com"))
                .andExpect(jsonPath("$[1].id").value(u2.getId().toString()))
                .andExpect(jsonPath("$[1].email").value("b@example.com"));
    }

    @Test
    void listReturnsUsers() throws Exception {
        var u1 = new AppUser(UUID.randomUUID(), "a@example.com", "hash", UserRole.CUSTOMER, Instant.EPOCH);
        var u2 = new AppUser(UUID.randomUUID(), "b@example.com", "hash", UserRole.CUSTOMER, Instant.EPOCH);
        when(userService.listUsers(eq(200))).thenReturn(List.of(u1, u2));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(u1.getId().toString()))
                .andExpect(jsonPath("$[0].email").value("a@example.com"))
                .andExpect(jsonPath("$[1].id").value(u2.getId().toString()))
                .andExpect(jsonPath("$[1].email").value("b@example.com"));
    }
}
