package com.lovingapp.loving.repository;

import com.lovingapp.loving.model.Ritual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RitualRepository extends JpaRepository<Ritual, Long> {
    // Custom query methods can be added here if needed
}
