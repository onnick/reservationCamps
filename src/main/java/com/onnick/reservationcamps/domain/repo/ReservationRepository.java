package com.onnick.reservationcamps.domain.repo;

import com.onnick.reservationcamps.domain.Reservation;
import com.onnick.reservationcamps.domain.ReservationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    Optional<Reservation> findBySessionIdAndUserId(UUID sessionId, UUID userId);

    @Query(
            """
            select r
            from Reservation r
            join fetch r.session s
            join fetch r.user u
            where u.id = :userId
            order by r.createdAt desc
            """)
    List<Reservation> findAllForUserWithJoins(@Param("userId") UUID userId);

    @Query(
            """
            select r
            from Reservation r
            join fetch r.session s
            join fetch r.user u
            order by r.createdAt desc
            """)
    List<Reservation> findAllWithJoins();

    long countBySessionIdAndStatus(UUID sessionId, ReservationStatus status);
}
