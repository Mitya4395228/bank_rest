package com.example.bankcards.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.bankcards.entity.enums.RoleType;

@Component
public class AuthenticationResolver {

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UserInfoDetails getUser() {
        return (UserInfoDetails) getAuthentication().getPrincipal();
    }

    public boolean userHasRole(RoleType role) {
        return getUser().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

}
