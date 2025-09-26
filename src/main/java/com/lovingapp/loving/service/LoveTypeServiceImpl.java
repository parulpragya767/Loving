package com.lovingapp.loving.service;

import com.lovingapp.loving.model.LoveTypeInfo;
import com.lovingapp.loving.repository.LoveTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoveTypeServiceImpl implements LoveTypeService {

    private final LoveTypeRepository loveTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LoveTypeInfo> findAll() {
        return loveTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LoveTypeInfo> findById(Integer id) {
        return loveTypeRepository.findById(id);
    }

    @Override
    @Transactional
    public LoveTypeInfo save(LoveTypeInfo loveTypeInfo) {
        return loveTypeRepository.save(loveTypeInfo);
    }

    @Override
    @Transactional
    public Optional<LoveTypeInfo> update(Integer id, LoveTypeInfo loveTypeInfo) {
        return loveTypeRepository.findById(id)
                .map(existingLoveType -> {
                    loveTypeInfo.setId(id); // Ensure the ID is set to the path variable
                    return loveTypeRepository.save(loveTypeInfo);
                });
    }

    @Override
    @Transactional
    public boolean deleteById(Integer id) {
        if (loveTypeRepository.existsById(id)) {
            loveTypeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
