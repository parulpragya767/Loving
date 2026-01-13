package com.lovingapp.loving.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lovingapp.loving.model.entity.LoveTypeInfo;
import com.lovingapp.loving.service.LoveTypeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/love-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class LoveTypeController {

    private final LoveTypeService loveTypeService;

    @GetMapping
    public ResponseEntity<List<LoveTypeInfo>> getAllLoveTypes() {
        log.info("Fetch love types request received");
        List<LoveTypeInfo> result = loveTypeService.findAll();
        log.info("Love types fetched successfully count={}", result == null ? 0 : result.size());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoveTypeInfo> getLoveTypeById(@PathVariable Integer id) {
        log.info("Fetch love type request received id={}", id);
        ResponseEntity<LoveTypeInfo> result = loveTypeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        log.info("Love type fetch completed id={} status={}", id, result.getStatusCode());
        return result;
    }
}
