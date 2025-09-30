package com.lovingapp.loving.repository;

import com.lovingapp.loving.model.Ritual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Collection;
import java.util.List;

@Repository
public interface RitualRepository extends JpaRepository<Ritual, UUID> {
    // Custom query methods can be added here if needed
    List<Ritual> findAllByTitleIn(Collection<String> titles);
}
