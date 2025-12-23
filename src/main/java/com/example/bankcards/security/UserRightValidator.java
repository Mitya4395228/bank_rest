package com.example.bankcards.security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.bankcards.entity.enums.RoleType;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Checking user rights to operate with cards
 */

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRightValidator {

    @Autowired
    AuthenticationResolver authenticationResolver;

    /**
     * Checking user rights to get card
     *
     * @param userId {@code UUID}
     * @return {@code boolean} true if user has a admin role or user is the card
     *         owner
     */
    public boolean isUserRightGetCard(UUID userId) {
        return authenticationResolver.userHasRole(RoleType.ADMIN) ? true
                : authenticationResolver.getUser().getId().equals(userId);
    }

}
