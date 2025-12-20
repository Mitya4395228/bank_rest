package com.example.bankcards.security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
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
        var user = authenticationResolver.getUser();
        if (isUserGetRole(user, RoleType.ADMIN)) {
            return true;
        }
        return user.getId().equals(userId);
    }

    private boolean isUserGetRole(UserInfoDetails user, RoleType role) {
        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()
                .contains("ROLE_" + RoleType.ADMIN.name());
    }

}
