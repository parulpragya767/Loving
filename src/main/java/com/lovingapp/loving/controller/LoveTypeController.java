package com.lovingapp.loving.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.model.LoveTypeInfo;
import com.lovingapp.loving.service.LoveTypeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/love-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoveTypeController {

    private final LoveTypeService loveTypeService;

    @GetMapping
    public ResponseEntity<List<LoveTypeInfo>> getAllLoveTypes() {
        return ResponseEntity.ok(loveTypeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoveTypeInfo> getLoveTypeById(@PathVariable Integer id) {
        return loveTypeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
