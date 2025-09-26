package com.lovingapp.loving.service;

import com.lovingapp.loving.model.LoveTypeInfo;

import java.util.List;
import java.util.Optional;

public interface LoveTypeService {
    List<LoveTypeInfo> findAll();
    Optional<LoveTypeInfo> findById(Integer id);
    LoveTypeInfo save(LoveTypeInfo loveTypeInfo);
    Optional<LoveTypeInfo> update(Integer id, LoveTypeInfo loveTypeInfo);
    boolean deleteById(Integer id);
}
