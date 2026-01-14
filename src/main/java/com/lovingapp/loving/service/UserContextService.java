package com.lovingapp.loving.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.mapper.UserContextMapper;
import com.lovingapp.loving.model.dto.UserContextDTO;
import com.lovingapp.loving.model.entity.UserContext;
import com.lovingapp.loving.repository.UserContextRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserContextService {

    private final UserContextRepository userContextRepository;

    @Transactional
    public UserContextDTO createUserContext(UserContextDTO userContextDTO) {
        UserContext userContext = UserContextMapper.toEntity(userContextDTO);
        UserContext savedContext = userContextRepository.saveAndFlush(userContext);
        return UserContextMapper.toDto(savedContext);
    }

    @Transactional(readOnly = true)
    public List<UserContextDTO> getUserContexts(UUID userId) {
        return userContextRepository.findByUserId(userId).stream()
                .map(UserContextMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserContextDTO getUserContext(String id) {
        return userContextRepository.findById(UUID.fromString(id))
                .map(UserContextMapper::toDto)
                .orElseThrow(() -> new RuntimeException("UserContext not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<UserContextDTO> getLatestUserContext(UUID userId) {
        return userContextRepository.findByUserId(userId).stream()
                .findFirst() // no ordering maintained now
                .map(UserContextMapper::toDto);
    }

    @Transactional
    public UserContextDTO updateUserContext(String id, UserContextDTO userContextDTO) {
        log.info("Updating user context userContextId={}", id);
        log.debug("Update user context payload userContextId={} payload={}", id, userContextDTO);
        UserContext existingContext = userContextRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("UserContext not found with id: " + id));

        UserContextMapper.updateEntityFromDto(existingContext, userContextDTO);

        UserContext updatedContext = userContextRepository.save(existingContext);
        log.info("User context updated successfully userContextId={}", updatedContext.getId());
        return UserContextMapper.toDto(updatedContext);
    }

    @Transactional
    public void deleteUserContext(String id) {
        log.info("Deleting user context userContextId={}", id);
        userContextRepository.deleteById(UUID.fromString(id));
    }
}