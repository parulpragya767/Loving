package com.lovingapp.loving.repository;

import com.lovingapp.loving.model.UserContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing UserContext entities.
 * Provides methods to retrieve and manage user context data.
 */
@Repository
public interface UserContextRepository extends JpaRepository<UserContext, UUID> {
    
    /**
     * Find all contexts for a specific user, ordered by last interaction time (newest first).
     * @param userId the ID of the user
     * @return list of user contexts
     */
    List<UserContext> findByUserIdOrderByLastInteractionAtDesc(String userId);
    
    /**
     * Find a specific context by user ID and conversation ID.
     * @param userId the ID of the user
     * @param conversationId the ID of the conversation
     * @return an Optional containing the user context if found
     */
    Optional<UserContext> findByUserIdAndConversationId(String userId, String conversationId);
    
    /**
     * Find the most recent context for a user.
     * @param userId the ID of the user
     * @return an Optional containing the most recent user context if found
     */
    @Query("SELECT uc FROM UserContext uc WHERE uc.userId = :userId ORDER BY uc.lastInteractionAt DESC")
    List<UserContext> findMostRecentByUserId(@Param("userId") String userId);
}
