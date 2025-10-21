package com.lovingapp.loving.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.lovingapp.loving.model.dto.RitualFilterRequest;
import com.lovingapp.loving.model.entity.Ritual;

public interface RitualRepositoryCustom {
    Page<Ritual> search(RitualFilterRequest filter, Pageable pageable);
}
