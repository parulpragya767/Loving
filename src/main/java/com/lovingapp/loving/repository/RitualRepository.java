package com.lovingapp.loving.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lovingapp.loving.model.entity.Ritual;

@Repository
public interface RitualRepository extends JpaRepository<Ritual, UUID>, RitualRepositoryCustom {
    // Custom query methods can be added here if needed
}
