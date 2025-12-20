package com.example.bankcards.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bankcards.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

}
