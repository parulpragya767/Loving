package com.lovingapp.loving.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<LoveTypeInfo> findById(Integer id) {
        return loveTypeRepository.findById(id);
    }

    @Transactional
    public LoveTypeInfo save(LoveTypeInfo loveTypeInfo) {
        log.info("Saving love type");
        log.debug("Save love type payload: {}", loveTypeInfo);
        return loveTypeRepository.save(loveTypeInfo);
    }

    @Transactional
    public Optional<LoveTypeInfo> update(Integer id, LoveTypeInfo loveTypeInfo) {
        log.info("Updating love type loveTypeId={}", id);
        log.debug("Update love type payload loveTypeId={} payload={}", id, loveTypeInfo);
        return loveTypeRepository.findById(id)
                .map(existingLoveType -> {
                    loveTypeInfo.setId(id); // Ensure the ID is set to the path variable
                    return loveTypeRepository.save(loveTypeInfo);
                });
    }

    @Transactional
    public boolean deleteById(Integer id) {
        log.info("Deleting love type loveTypeId={}", id);
        if (loveTypeRepository.existsById(id)) {
            loveTypeRepository.deleteById(id);
            log.info("Love type deleted successfully loveTypeId={}", id);
            return true;
        }
        log.info("Love type delete skipped: not found loveTypeId={}", id);
        return false;
    }
}
