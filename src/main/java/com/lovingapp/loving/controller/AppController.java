package com.lovingapp.loving.controller;

import com.lovingapp.loving.dto.LoveTypeDTO;
import com.lovingapp.loving.dto.RitualDTO;
import com.lovingapp.loving.model.LoveType;
import com.lovingapp.loving.model.Ritual;
import com.lovingapp.loving.repository.LoveTypeRepository;
import com.lovingapp.loving.repository.RitualRepository;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class AppController {

    private final RitualRepository ritualRepository;
    private final LoveTypeRepository loveTypeRepository;

    @Autowired
    public AppController(RitualRepository ritualRepository, 
                        LoveTypeRepository loveTypeRepository) {
        this.ritualRepository = ritualRepository;
        this.loveTypeRepository = loveTypeRepository;
    }

    @GetMapping("/love-types")
    public List<LoveTypeDTO> getLoveTypes() {
        return loveTypeRepository.findAll().stream()
                .map(LoveTypeDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/rituals")
    public List<RitualDTO> getRituals() {
        return ritualRepository.findAll().stream()
                .map(RitualDTO::new)
                .collect(Collectors.toList());
    }

}
