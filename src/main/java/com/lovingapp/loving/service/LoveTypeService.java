package com.lovingapp.loving.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.model.entity.LoveTypeInfo;
import com.lovingapp.loving.repository.LoveTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoveTypeService {

    private final LoveTypeRepository loveTypeRepository;

    @Transactional(readOnly = true)
    public List<LoveTypeInfo> findAll() {
        return loveTypeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public LoveTypeInfo findById(Integer id) {
        return loveTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoveType", "id", id));
    }

    @Transactional
    public LoveTypeInfo save(LoveTypeInfo loveTypeInfo) {
        log.info("Saving love type id={} : {}", loveTypeInfo.getId(), loveTypeInfo.getLoveType());

        LoveTypeInfo savedLoveType = loveTypeRepository.save(loveTypeInfo);

        log.info("Love type saved successfully id={} : {}", savedLoveType.getId(), savedLoveType.getLoveType());
        return savedLoveType;
    }

    @Transactional
    public LoveTypeInfo update(Integer id, LoveTypeInfo loveTypeInfo) {
        log.info("Updating love type id={} : {}", id, loveTypeInfo.getLoveType());

        LoveTypeInfo updatedLoveType = loveTypeRepository.findById(id)
                .map(existingLoveType -> {
                    loveTypeInfo.setId(id); // Ensure the ID is set to the path variable
                    return loveTypeRepository.save(loveTypeInfo);
                })
                .orElseThrow(() -> new ResourceNotFoundException("LoveType", "id", id));

        log.info("Love type updated successfully id={} : {}", updatedLoveType.getId(), updatedLoveType.getLoveType());
        return updatedLoveType;
    }
}
