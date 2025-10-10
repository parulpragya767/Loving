package com.lovingapp.loving.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.dto.UserContextDTO;
import com.lovingapp.loving.mapper.UserContextMapper;
import com.lovingapp.loving.model.UserContext;
import com.lovingapp.loving.repository.UserContextRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UserContextRepository userContextRepository;

    @Transactional
    public UserContextDTO createUserContext(UserContextDTO userContextDTO) {
        UserContext userContext = UserContextMapper.toEntity(userContextDTO);
        UserContext savedContext = userContextRepository.save(userContext);
        return UserContextMapper.toDto(savedContext);
    }

    @Transactional(readOnly = true)
    public List<UserContextDTO> getUserContexts(UUID userId) {
        return userContextRepository.findByUserIdOrderByLastInteractionAtDesc(userId).stream()
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
        return userContextRepository.findByUserIdOrderByLastInteractionAtDesc(userId).stream()
                .findFirst()
                .map(UserContextMapper::toDto);
    }

    @Transactional
    public UserContextDTO updateUserContext(String id, UserContextDTO userContextDTO) {
        UserContext existingContext = userContextRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new RuntimeException("UserContext not found with id: " + id));

        UserContextMapper.updateEntityFromDto(existingContext, userContextDTO);

        UserContext updatedContext = userContextRepository.save(existingContext);
        return UserContextMapper.toDto(updatedContext);
    }

    @Transactional
    public void deleteUserContext(String id) {
        userContextRepository.deleteById(UUID.fromString(id));
    }
}