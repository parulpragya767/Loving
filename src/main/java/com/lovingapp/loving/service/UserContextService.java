package com.lovingapp.loving.service;

import com.lovingapp.loving.dto.UserContextDTO;
import com.lovingapp.loving.mapper.UserContextMapper;
import com.lovingapp.loving.model.UserContext;
import com.lovingapp.loving.repository.UserContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing user contexts, which capture the user's current state,
 * preferences, and situational context for ritual matching.
 */
@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UserContextRepository userContextRepository;
    private final UserContextMapper userContextMapper;

    @Transactional
    public UserContextDTO createUserContext(UserContextDTO userContextDTO) {
        UserContext userContext = userContextMapper.toEntity(userContextDTO);
        UserContext savedContext = userContextRepository.save(userContext);
        return userContextMapper.toDto(savedContext);
    }

    @Transactional(readOnly = true)
    public List<UserContextDTO> getUserContexts(UUID userId) {
        return userContextRepository.findByUserIdOrderByLastInteractionAtDesc(userId).stream()
                .map(userContextMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserContextDTO getUserContext(String id) {
        return userContextRepository.findById(UUID.fromString(id))
                .map(userContextMapper::toDto)
                .orElseThrow(() -> new RuntimeException("UserContext not found with id: " + id));
    }

    @Transactional
    public UserContextDTO updateUserContext(String id, UserContextDTO userContextDTO) {
        UserContext existingContext = userContextRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("UserContext not found with id: " + id));
        
        userContextMapper.updateEntityFromDto(existingContext, userContextDTO);
        existingContext.setLastInteractionAt(java.time.OffsetDateTime.now());
        
        UserContext updatedContext = userContextRepository.save(existingContext);
        return userContextMapper.toDto(updatedContext);
    }

    @Transactional
    public void deleteUserContext(String id) {
        userContextRepository.deleteById(UUID.fromString(id));
    }

    @Transactional(readOnly = true)
    public Optional<UserContextDTO> getActiveUserContext(UUID userId) {
        return userContextRepository.findMostRecentByUserId(userId).stream()
                .findFirst()
                .map(userContextMapper::toDto);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserContextDTO> getLatestUserContext(UUID userId) {
        return userContextRepository.findMostRecentByUserId(userId).stream()
                .findFirst()
                .map(userContextMapper::toDto);
    }
}
