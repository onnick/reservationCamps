package com.onnick.reservationcamps.domain.repo;

import com.onnick.reservationcamps.domain.CampSession;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CampSessionRepository extends MongoRepository<CampSession, UUID> {
    List<CampSession> findAllByCampIdOrderByStartDateAsc(UUID campId);
}
