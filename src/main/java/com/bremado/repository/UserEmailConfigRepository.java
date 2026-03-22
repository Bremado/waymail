package com.bremado.repository;

import com.bremado.model.UserEmailConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserEmailConfigRepository extends MongoRepository<UserEmailConfig, String> {
    Optional<UserEmailConfig> findByUser(String user);
}
