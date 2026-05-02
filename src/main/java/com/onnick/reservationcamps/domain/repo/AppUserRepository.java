package com.onnick.reservationcamps.domain.repo;

import com.onnick.reservationcamps.domain.AppUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AppUserRepository extends MongoRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);

    List<AppUser> findTop20ByEmailContainingIgnoreCaseOrderByEmailAsc(String q);

    List<AppUser> findTop20ByOrderByEmailAsc();
}
