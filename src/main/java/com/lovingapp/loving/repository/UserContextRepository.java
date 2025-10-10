package com.lovingapp.loving.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lovingapp.loving.model.UserContext;

@Repository
public interface UserContextRepository extends JpaRepository<UserContext, UUID> {

    List<UserContext> findByUserIdOrderByLastInteractionAtDesc(UUID userId);

    Optional<UserContext> findByUserIdAndConversationId(UUID userId, String conversationId);
}
