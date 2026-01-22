package com.lovingapp.loving.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lovingapp.loving.model.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByAuthUserId(UUID authUserId);

    Boolean existsByEmail(String email);
}
