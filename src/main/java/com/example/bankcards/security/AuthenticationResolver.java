package com.example.bankcards.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.bankcards.entity.enums.RoleType;

/**
 * Getting information about the current user
 */

@Component
public class AuthenticationResolver {

    /**
     * Get the current user's authentication
     * 
     * @return {@code Authentication} the current user
     */
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Get the current user's information
     * 
     * @return {@code UserInfoDetails} the current user
     */
    public UserInfoDetails getUser() {
        return (UserInfoDetails) getAuthentication().getPrincipal();
    }

    /**
     * Check if the current user has a specific role
     * 
     * @param role {@code RoleType} the role to check
     * @return {@code true} if the current user has the role, {@code false}
     *         otherwise
     */
    public boolean userHasRole(RoleType role) {
        return getUser().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
    }

}
