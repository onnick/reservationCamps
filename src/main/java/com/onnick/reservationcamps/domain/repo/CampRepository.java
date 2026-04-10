package com.onnick.reservationcamps.domain.repo;

import com.onnick.reservationcamps.domain.Camp;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampRepository extends JpaRepository<Camp, UUID> {}

