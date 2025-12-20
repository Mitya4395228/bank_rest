package com.example.bankcards.security;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.bankcards.entity.UserEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoDetails implements UserDetails {

    UUID id;
    String username;
    String password;
    boolean accountNonExpired;
    boolean enabled;
    boolean accountNonLocked;
    boolean credentialsNonExpired;
    List<? extends GrantedAuthority> authorities;

    public UserInfoDetails(UserEntity user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.accountNonExpired = user.isAccountNonExpired();
        this.enabled = user.isEnabled();
        this.accountNonLocked = user.isAccountNonLocked();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.authorities = user.getRoles().stream().map(m -> new SimpleGrantedAuthority("ROLE_" + m.getRole().name()))
                .toList();
    }

}
