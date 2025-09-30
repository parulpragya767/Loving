package com.lovingapp.loving.service;

import com.lovingapp.loving.dto.RitualPackDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RitualPackService {
    List<RitualPackDTO> findAll();
    Optional<RitualPackDTO> findById(UUID id);
    RitualPackDTO create(RitualPackDTO dto);
    Optional<RitualPackDTO> update(UUID id, RitualPackDTO dto);
    boolean deleteById(UUID id);
}
