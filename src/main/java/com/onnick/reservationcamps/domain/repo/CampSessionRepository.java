package com.onnick.reservationcamps.domain.repo;

import com.onnick.reservationcamps.domain.CampSession;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampSessionRepository extends JpaRepository<CampSession, UUID> {
    List<CampSession> findAllByCamp_IdOrderByStartDateAsc(UUID campId);
}
