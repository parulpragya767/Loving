package com.lovingapp.loving.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.UserMapper;
import com.lovingapp.loving.model.dto.UserDTOs.UserDTO;
import com.lovingapp.loving.model.dto.UserDTOs.UserUpdateRequest;
import com.lovingapp.loving.model.entity.User;
import com.lovingapp.loving.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDTO syncUser(UUID authUserId, String email) {
        log.info("Syncing user profile from auth context");
        return userRepository.findByAuthUserId(authUserId)
                .map(existing -> {
                    existing.setLastLoginAt(OffsetDateTime.now());
                    if (email != null && !email.equals(existing.getEmail())) {
                        existing.setEmail(email);
                    }
                    User saved = userRepository.save(existing);
                    log.info("User profile synced successfully");
                    return UserMapper.toDto(saved);
                })
                .orElseGet(() -> {
                    log.info("Creating new user profile for first login");
                    User newUser = User.builder()
                            .authUserId(authUserId)
                            .email(email)
                            .lastLoginAt(OffsetDateTime.now())
                            .build();
                    User saved = userRepository.save(newUser);
                    log.info("User profile created successfully");
                    return UserMapper.toDto(saved);
                });
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByAuthUserId(UUID authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with authUserId: " + authUserId));
    }

    @Transactional
    public UserDTO updateUser(UUID userId, UserUpdateRequest request) {
        log.info("Updating user profile");
        return userRepository.findById(userId)
                .map(existingUser -> {
                    UserMapper.updateEntity(request, existingUser);
                    User updatedUser = userRepository.save(existingUser);
                    log.info("User profile updated successfully");
                    return UserMapper.toDto(updatedUser);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
