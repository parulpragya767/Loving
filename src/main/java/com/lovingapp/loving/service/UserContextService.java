package com.lovingapp.loving.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.UserContextMapper;
import com.lovingapp.loving.model.dto.UserContextDTOs.UserContextCreateRequest;
import com.lovingapp.loving.model.dto.UserContextDTOs.UserContextDTO;
import com.lovingapp.loving.model.entity.UserContext;
import com.lovingapp.loving.repository.UserContextRepository;
import com.lovingapp.loving.service.chat.AIChatSessionPersistenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserContextService {

    private final UserContextRepository userContextRepository;
    private final AIChatSessionPersistenceService chatSessionPersistenceService;

    @Transactional(readOnly = true)
    public List<UserContextDTO> findAll(UUID userId) {
        return userContextRepository.findByUserId(userId).stream()
                .map(UserContextMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserContextDTO> findByConversationId(UUID userId, UUID conversationId) {
        return userContextRepository.findByUserIdAndConversationId(userId, conversationId)
                .stream()
                .map(UserContextMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserContextDTO create(UUID userId, UserContextCreateRequest request) {

        if (request.getConversationId() != null) {
            chatSessionPersistenceService.findSessionByIdAndUserId(request.getConversationId(), userId);
        }

        UserContext userContext = UserContext.builder()
                .userId(userId)
                .conversationId(request.getConversationId())
                .journey(request.getJourney())
                .loveTypes(Objects.requireNonNullElse(request.getLoveTypes(), Collections.emptyList()))
                .relationalNeeds(Objects.requireNonNullElse(request.getRelationalNeeds(), Collections.emptyList()))
                .relationshipStatus(request.getRelationshipStatus())
                .semanticSummary(request.getSemanticSummary())
                .build();

        UserContext savedContext = userContextRepository.saveAndFlush(userContext);
        return UserContextMapper.toDto(savedContext);
    }

    @Transactional
    public void delete(UUID id) {
        userContextRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserContext", "id", id));
        userContextRepository.deleteById(id);
    }
}