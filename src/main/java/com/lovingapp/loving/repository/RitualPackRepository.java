package com.lovingapp.loving.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lovingapp.loving.model.RitualPack;

@Repository
public interface RitualPackRepository extends JpaRepository<RitualPack, UUID> {
    Optional<RitualPack> findByTitle(String title);

    List<RitualPack> findAllByTitleIn(List<String> titles);
}
