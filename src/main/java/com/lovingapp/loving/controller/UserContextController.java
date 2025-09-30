package com.lovingapp.loving.controller;

import com.lovingapp.loving.model.dto.UserContextDTO;
import com.lovingapp.loving.service.UserContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<UserContextDTO>> getUserContexts(@RequestParam String userId) {
        return ResponseEntity.ok(userContextService.getUserContexts(userId));
    }

    @GetMapping("/active")
    public ResponseEntity<UserContextDTO> getActiveUserContext(@RequestParam String userId) {
        UserContextDTO activeContext = userContextService.getActiveUserContext(userId);
        return activeContext != null ? 
            ResponseEntity.ok(activeContext) : 
            ResponseEntity.notFound().build();
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
