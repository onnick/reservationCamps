package com.onnick.reservationcamps.domain.repo;

import com.onnick.reservationcamps.domain.CampSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampSessionRepository extends JpaRepository<CampSession, UUID> {}

