package com.example.bankcards.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;

public interface CardRepository extends JpaRepository<CardEntity, UUID>, CardRepositoryCustom {

    boolean existsByNumber(String number);

    List<CardEntity> findAllByUserId(UUID userId);

    @Query("select c.id from CardEntity c where c.expirationDate < :expirationDate and status != :status")
    Stream<UUID> findAllIdWithExpiredDate(LocalDate expirationDate, CardStatus status);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Modifying
    @Query("update CardEntity c set c.status = :status where c.id = :id")
    void updateStatus(UUID id, CardStatus status);

}
