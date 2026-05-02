package com.onnick.reservationcamps.domain.repo;

import com.onnick.reservationcamps.domain.Reservation;
import com.onnick.reservationcamps.domain.ReservationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReservationRepository extends MongoRepository<Reservation, UUID> {
    Optional<Reservation> findBySessionIdAndUserId(UUID sessionId, UUID userId);

    List<Reservation> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    long countBySessionIdAndStatus(UUID sessionId, ReservationStatus status);
}
