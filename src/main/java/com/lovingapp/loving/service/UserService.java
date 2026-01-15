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

    /*
     * Returns the user object associated with the authUserId (which is the external
     * supabase user id).
     * If there is no user found, it will create a new user object and return it.
     */
    @Transactional
    public UserDTO syncUser(UUID authUserId, String email) {
        return userRepository.findByAuthUserId(authUserId)
                .map(existing -> {
                    existing.setLastLoginAt(OffsetDateTime.now());
                    if (email != null && !email.equals(existing.getEmail())) {
                        existing.setEmail(email);
                    }
                    User saved = userRepository.save(existing);
                    return UserMapper.toDto(saved);
                })
                .orElseGet(() -> {
                    log.info("Creating new user profile for first login authUserId={}", authUserId);
                    User newUser = User.builder()
                            .authUserId(authUserId)
                            .email(email)
                            .lastLoginAt(OffsetDateTime.now())
                            .build();
                    User saved = userRepository.save(newUser);
                    return UserMapper.toDto(saved);
                });
    }

    /*
     * Returns the user object associated with the authUserId (which is the external
     * supabase user id).
     * If there is no user found, it will throw a ResourceNotFoundException.
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByAuthUserId(UUID authUserId) {
        return userRepository.findByAuthUserId(authUserId)
                .map(UserMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "authUserId", authUserId));
    }

    @Transactional
    public void updateUser(UUID userId, UserUpdateRequest request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserMapper.updateEntity(request, existingUser);
        userRepository.save(existingUser);
    }
}
