package com.lovingapp.loving.controller;

import com.lovingapp.loving.dto.RitualHistoryDTO;
import com.lovingapp.loving.mapper.RitualHistoryMapper;
import com.lovingapp.loving.model.RitualHistory;
import com.lovingapp.loving.repository.RitualHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/ritual-history")
public class RitualHistoryController {

    private final RitualHistoryRepository ritualHistoryRepository;

    private UUID getAuthUserId(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not present");
        }
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user id in token");
        }
    }

    @GetMapping
    public ResponseEntity<List<RitualHistoryDTO>> list(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(value = "ritualId", required = false) UUID ritualId
    ) {
        UUID userId = getAuthUserId(jwt);

        List<RitualHistory> list;
        if (ritualId != null) {
            list = ritualHistoryRepository.findByUserIdAndRitualIdOrderByOccurredAtDesc(userId, ritualId);
        } else {
            list = ritualHistoryRepository.findByUserIdOrderByOccurredAtDesc(userId);
        }
        List<RitualHistoryDTO> body = list.stream().map(RitualHistoryMapper::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RitualHistoryDTO> getById(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID id) {
        UUID userId = getAuthUserId(jwt);
        return ritualHistoryRepository.findById(id)
                .map(entity -> {
                    if (!entity.getUserId().equals(userId)) {
                        return new ResponseEntity<RitualHistoryDTO>(HttpStatus.FORBIDDEN);
                    }
                    return ResponseEntity.ok(RitualHistoryMapper.toDto(entity));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RitualHistoryDTO> create(@AuthenticationPrincipal Jwt jwt, @RequestBody RitualHistoryDTO request) {
        UUID userId = getAuthUserId(jwt);
        request.setId(null);
        RitualHistory entity = RitualHistoryMapper.fromDto(request);
        // Enforce ownership from token
        entity.setUserId(userId);
        RitualHistory saved = ritualHistoryRepository.save(entity);
        RitualHistoryDTO body = RitualHistoryMapper.toDto(saved);
        return ResponseEntity.created(URI.create("/api/ritual-history/" + saved.getId())).body(body);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RitualHistoryDTO> update(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID id, @RequestBody RitualHistoryDTO request) {
        UUID userId = getAuthUserId(jwt);
        return ritualHistoryRepository.findById(id)
                .map(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        return new ResponseEntity<RitualHistoryDTO>(HttpStatus.FORBIDDEN);
                    }
                    request.setId(existing.getId());
                    RitualHistoryMapper.updateEntityFromDto(request, existing);
                    // Enforce ownership from token
                    existing.setUserId(userId);
                    RitualHistory saved = ritualHistoryRepository.save(existing);
                    return ResponseEntity.ok(RitualHistoryMapper.toDto(saved));
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID id) {
        UUID userId = getAuthUserId(jwt);
        return ritualHistoryRepository.findById(id)
                .map(existing -> {
                    if (!existing.getUserId().equals(userId)) {
                        return new ResponseEntity<Void>(HttpStatus.FORBIDDEN);
                    }
                    ritualHistoryRepository.delete(existing);
                    return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
                })
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
