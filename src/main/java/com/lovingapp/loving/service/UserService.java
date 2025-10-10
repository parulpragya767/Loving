package com.lovingapp.loving.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.loving.exception.ResourceNotFoundException;
import com.lovingapp.loving.mapper.UserMapper;
import com.lovingapp.loving.model.dto.UserDTO;
import com.lovingapp.loving.model.entity.User;
import com.lovingapp.loving.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(String id) {
        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return UserMapper.toDto(user);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        User user = UserMapper.toEntity(userDTO);
        User savedUser = userRepository.save(user);
        return UserMapper.toDto(savedUser);
    }

    @Transactional
    public UserDTO updateUser(String id, UserDTO userDTO) {
        return userRepository.findById(UUID.fromString(id))
                .map(existingUser -> {
                    userDTO.setId(id); // Ensure ID consistency
                    UserMapper.updateEntityFromDto(userDTO, existingUser);
                    User updatedUser = userRepository.save(existingUser);
                    return UserMapper.toDto(updatedUser);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(UUID.fromString(id))) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(UUID.fromString(id));
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
