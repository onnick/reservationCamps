package com.onnick.reservationcamps.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.onnick.reservationcamps.api.dto.CreateCampRequest;
import com.onnick.reservationcamps.api.dto.CreateReservationRequest;
import com.onnick.reservationcamps.api.dto.CreateSessionRequest;
import com.onnick.reservationcamps.api.dto.CreateUserRequest;
import com.onnick.reservationcamps.api.dto.IdResponse;
import com.onnick.reservationcamps.api.dto.ReservationResponse;
import com.onnick.reservationcamps.api.dto.UserReservationRowResponse;
import com.onnick.reservationcamps.domain.ReservationStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@Import(ReservationFlowIT.FixedClockConfig.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationFlowIT {
    @Container
    static final MongoDBContainer mongo =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock testClock() {
            return Clock.fixed(Instant.parse("2026-04-10T10:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired private TestRestTemplate http;

    @Test
    void reservationCanBeCreatedConfirmedAndPaid() {
        var userId = createUser("a@example.com");
        var campId = createCampAsAdmin("Camp", 1000);
        var sessionId =
                createSessionAsAdmin(
                        campId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 10);

        var reservation = createReservation(sessionId, userId);
        assertThat(reservation.status()).isEqualTo(ReservationStatus.CREATED);

        var confirmed = post("/api/reservations/" + reservation.id() + "/confirm", null, ReservationResponse.class);
        assertThat(confirmed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirmed.getBody().status()).isEqualTo(ReservationStatus.CONFIRMED);

        var paid = post("/api/reservations/" + reservation.id() + "/pay", null, ReservationResponse.class);
        assertThat(paid.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(paid.getBody().status()).isEqualTo(ReservationStatus.PAID);

        var cancel = post("/api/reservations/" + reservation.id() + "/cancel", null, String.class);
        assertThat(cancel.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void reservationsCanBeListedForUser() {
        var userId = createUser("list@example.com");
        var campId = createCampAsAdmin("Camp", 1000);
        var sessionId =
                createSessionAsAdmin(
                        campId, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7), 10);

        createReservation(sessionId, userId);

        var response = http.getForEntity("/api/users/" + userId + "/reservations", UserReservationRowResponse[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThanOrEqualTo(1);
        assertThat(response.getBody()[0].campName()).isEqualTo("Camp");
        assertThat(response.getBody()[0].startDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(response.getBody()[0].endDate()).isEqualTo(LocalDate.of(2026, 5, 7));
        assertThat(response.getBody()[0].status()).isEqualTo(ReservationStatus.CREATED);
    }

    private UUID createUser(String email) {
        var response =
                post(
                        "/api/users",
                        new CreateUserRequest(email, "password123", null),
                        IdResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().id();
    }

    private UUID createCampAsAdmin(String name, int basePriceCents) {
        var headers = adminHeaders();
        var entity = new HttpEntity<>(new CreateCampRequest(name, basePriceCents), headers);
        var response = http.exchange("/api/camps", HttpMethod.POST, entity, IdResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().id();
    }

    private UUID createSessionAsAdmin(UUID campId, LocalDate startDate, LocalDate endDate, int capacity) {
        var headers = adminHeaders();
        var entity = new HttpEntity<>(new CreateSessionRequest(startDate, endDate, capacity), headers);
        var response =
                http.exchange("/api/camps/" + campId + "/sessions", HttpMethod.POST, entity, IdResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody().id();
    }

    private ReservationResponse createReservation(UUID sessionId, UUID userId) {
        var response =
                post(
                        "/api/sessions/" + sessionId + "/reservations",
                        new CreateReservationRequest(userId),
                        ReservationResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return response.getBody();
    }

    private HttpHeaders adminHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Actor-Role", "ADMIN");
        return headers;
    }

    private <T> org.springframework.http.ResponseEntity<T> post(String path, Object body, Class<T> responseType) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var entity = new HttpEntity<>(body, headers);
        return http.exchange(path, HttpMethod.POST, entity, responseType);
    }
}
