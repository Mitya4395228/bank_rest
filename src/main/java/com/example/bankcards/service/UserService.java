package com.example.bankcards.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.repository.UserRepository;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class UserService {

    @Autowired
    UserRepository repository;

    public UserEntity getEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id=%s".formatted(id)));
    }

}
