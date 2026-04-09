package com.lovingapp.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.lovingapp.model.entity.RitualPack;

@Repository
public interface RitualPackRepository extends JpaRepository<RitualPack, UUID> {

    @Query("SELECT DISTINCT rp FROM RitualPack rp " +
            "LEFT JOIN FETCH rp.ritualPackRituals rpr " +
            "LEFT JOIN FETCH rpr.ritual")
    List<RitualPack> findAllWithRituals();

    @Query("SELECT rp FROM RitualPack rp " +
            "LEFT JOIN FETCH rp.ritualPackRituals rpr " +
            "LEFT JOIN FETCH rpr.ritual " +
            "WHERE rp.id = :id")
    Optional<RitualPack> findByIdWithRituals(UUID id);

    @Query("SELECT DISTINCT rp FROM RitualPack rp " +
            "LEFT JOIN FETCH rp.ritualPackRituals rpr " +
            "LEFT JOIN FETCH rpr.ritual " +
            "WHERE rp.id IN :ids")
    List<RitualPack> findAllByIdWithRituals(List<UUID> ids);
}
