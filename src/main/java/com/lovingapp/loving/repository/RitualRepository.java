package com.lovingapp.loving.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lovingapp.loving.model.Ritual;

@Repository
public interface RitualRepository extends JpaRepository<Ritual, UUID> {
    // Custom query methods can be added here if needed
    List<Ritual> findAllByTitleIn(Collection<String> titles);
}
