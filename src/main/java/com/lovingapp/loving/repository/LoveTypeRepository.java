package com.lovingapp.loving.repository;

import com.lovingapp.loving.model.LoveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoveTypeRepository extends JpaRepository<LoveType, Long> {
    // Custom query methods can be added here if needed
}
