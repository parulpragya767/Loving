package com.lovingapp.loving.controller;

import com.lovingapp.loving.dto.RitualHistoryDTO;
import com.lovingapp.loving.mapper.RitualHistoryMapper;
import com.lovingapp.loving.model.RitualHistory;
import com.lovingapp.loving.repository.RitualHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/ritual-histories")
public class RitualHistoryController {

    private final RitualHistoryRepository ritualHistoryRepository;

    @GetMapping
    public List<RitualHistoryDTO> list(
            @RequestParam(value = "userId", required = false) UUID userId,
            @RequestParam(value = "ritualId", required = false) UUID ritualId
    ) {
        List<RitualHistory> list;
        if (userId != null && ritualId != null) {
            list = ritualHistoryRepository.findByUserIdAndRitualIdOrderByOccurredAtDesc(userId, ritualId);
        } else if (userId != null) {
            list = ritualHistoryRepository.findByUserIdOrderByOccurredAtDesc(userId);
        } else if (ritualId != null) {
            list = ritualHistoryRepository.findByRitualIdOrderByOccurredAtDesc(ritualId);
        } else {
            list = ritualHistoryRepository.findAll();
        }
        return list.stream().map(RitualHistoryMapper::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RitualHistoryDTO> getById(@PathVariable("id") UUID id) {
        return ritualHistoryRepository.findById(id)
                .map(RitualHistoryMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RitualHistoryDTO> create(@RequestBody RitualHistoryDTO request) {
        request.setId(null);
        RitualHistory entity = RitualHistoryMapper.fromDto(request);
        RitualHistory saved = ritualHistoryRepository.save(entity);
        RitualHistoryDTO body = RitualHistoryMapper.toDto(saved);
        return ResponseEntity.created(URI.create("/api/ritual-histories/" + saved.getId())).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RitualHistoryDTO> update(@PathVariable("id") UUID id, @RequestBody RitualHistoryDTO request) {
        return ritualHistoryRepository.findById(id)
                .map(existing -> {
                    request.setId(existing.getId());
                    RitualHistoryMapper.updateEntityFromDto(request, existing);
                    RitualHistory saved = ritualHistoryRepository.save(existing);
                    return ResponseEntity.ok(RitualHistoryMapper.toDto(saved));
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        return ritualHistoryRepository.findById(id)
                .map(existing -> {
                    ritualHistoryRepository.delete(existing);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
