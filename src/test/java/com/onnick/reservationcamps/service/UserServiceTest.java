package com.onnick.reservationcamps.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.onnick.reservationcamps.domain.UserRole;
import com.onnick.reservationcamps.domain.error.BusinessRuleViolationException;
import com.onnick.reservationcamps.domain.repo.AppUserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    private static final Instant NOW = Instant.parse("2026-04-10T10:00:00Z");

    @Mock private AppUserRepository userRepository;

    @Test
    void createsUserWithNormalizedEmail() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service = new UserService(userRepository, clock);

        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(org.mockito.Mockito.any()))
                .thenAnswer(inv -> inv.getArgument(0));

        var user = service.createUser(" A@Example.com ", UserRole.CUSTOMER);

        assertThat(user.getEmail()).isEqualTo("a@example.com");
        assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(user.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsDuplicateEmail() {
        var clock = Clock.fixed(NOW, ZoneOffset.UTC);
        var service = new UserService(userRepository, clock);

        when(userRepository.findByEmail("a@example.com"))
                .thenReturn(Optional.of(new com.onnick.reservationcamps.domain.AppUser(UUID.randomUUID(), "a@example.com", UserRole.CUSTOMER, NOW)));

        assertThatThrownBy(() -> service.createUser("a@example.com", UserRole.CUSTOMER))
                .isInstanceOf(BusinessRuleViolationException.class)
                .satisfies(
                        ex ->
                                assertThat(((BusinessRuleViolationException) ex).getCode())
                                        .isEqualTo("user.email.duplicate"));
    }
}
