package com.onnick.reservationcamps.domain.repo;

import com.onnick.reservationcamps.domain.Camp;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CampRepository extends MongoRepository<Camp, UUID> {}
