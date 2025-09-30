package com.lovingapp.loving.repository;

import com.lovingapp.loving.model.RitualPack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RitualPackRepository extends JpaRepository<RitualPack, UUID> {
}
