package com.onnick.reservationcamps.domain.repo;

import com.onnick.reservationcamps.domain.Reservation;
import com.onnick.reservationcamps.domain.ReservationStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Optional<Reservation> findBySessionIdAndUserId(UUID sessionId, UUID userId);

    long countBySessionIdAndStatus(UUID sessionId, ReservationStatus status);
}

