package com.example.bankcards.security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.bankcards.entity.enums.RoleType;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRightValidator {

    @Autowired
    AuthenticationResolver authenticationResolver;

    public boolean isUserRightGetCard(UUID userId) {
        return authenticationResolver.userHasRole(RoleType.ADMIN) ? true
                : authenticationResolver.getUser().getId().equals(userId);
    }

}
