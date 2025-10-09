package com.lovingapp.loving.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lovingapp.loving.model.LoveTypeInfo;

@Repository
public interface LoveTypeRepository extends JpaRepository<LoveTypeInfo, Integer> {
    // Custom query methods can be added here if needed
}
