package com.lovingapp.loving.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lovingapp.loving.model.RitualPack;

@Repository
public interface RitualPackRepository extends JpaRepository<RitualPack, UUID> {
}
