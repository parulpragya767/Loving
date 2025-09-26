package com.lovingapp.loving.controller;

import com.lovingapp.loving.model.LoveTypeInfo;
import com.lovingapp.loving.service.LoveTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    public ResponseEntity<LoveTypeInfo> createLoveType(@Valid @RequestBody LoveTypeInfo loveTypeInfo) {
        LoveTypeInfo createdLoveType = loveTypeService.save(loveTypeInfo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLoveType);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoveTypeInfo> updateLoveType(
            @PathVariable Integer id, 
            @Valid @RequestBody LoveTypeInfo loveTypeInfo) {
        return loveTypeService.update(id, loveTypeInfo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoveType(@PathVariable Integer id) {
        if (loveTypeService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
