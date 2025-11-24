package com.lovingapp.loving.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lovingapp.loving.model.entity.RitualRecommendation;

@Repository
public interface RitualRecommendationRepository extends JpaRepository<RitualRecommendation, UUID> {
    // Custom query methods can be added here if needed
    List<RitualRecommendation> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
