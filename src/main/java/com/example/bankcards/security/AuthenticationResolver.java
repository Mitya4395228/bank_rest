package com.example.bankcards.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationResolver {

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UserInfoDetails getUser() {
        return (UserInfoDetails) getAuthentication().getPrincipal();
    }

}
