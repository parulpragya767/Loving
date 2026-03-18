package com.lovingapp.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lovingapp.model.entity.RitualPackRitual;
import com.lovingapp.model.entity.RitualPackRitualId;

@Repository
public interface RitualPackRitualRepository extends JpaRepository<RitualPackRitual, RitualPackRitualId> {

    @Query("""
                SELECT rpr FROM RitualPackRitual rpr
                WHERE rpr.ritualPack.id = :packId
                ORDER BY rpr.position ASC
            """)
    List<RitualPackRitual> findByPackIdOrdered(@Param("packId") UUID packId);

    void deleteByRitualPackId(UUID packId);
}
