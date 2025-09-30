package com.lovingapp.loving.controller;

import com.lovingapp.loving.model.dto.UserContextDTO;
import com.lovingapp.loving.service.UserContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/user-contexts")
@RequiredArgsConstructor
public class UserContextController {

    private final UserContextService userContextService;

    @PostMapping
    public ResponseEntity<UserContextDTO> createUserContext(@Valid @RequestBody UserContextDTO userContextDTO) {
        return ResponseEntity.ok(userContextService.createUserContext(userContextDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserContextDTO> getUserContext(@PathVariable String id) {
        return ResponseEntity.ok(userContextService.getUserContext(id));
    }

    @GetMapping
    public ResponseEntity<List<UserContextDTO>> getUserContexts(@RequestParam UUID userId) {
        return ResponseEntity.ok(userContextService.getUserContexts(userId));
    }

    @GetMapping("/active")
    public ResponseEntity<UserContextDTO> getActiveUserContext(@RequestParam UUID userId) {
        return userContextService.getActiveUserContext(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserContextDTO> updateUserContext(
            @PathVariable String id,
            @Valid @RequestBody UserContextDTO userContextDTO) {
        return ResponseEntity.ok(userContextService.updateUserContext(id, userContextDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserContext(@PathVariable String id) {
        userContextService.deleteUserContext(id);
        return ResponseEntity.noContent().build();
    }
}
