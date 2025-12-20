package com.example.bankcards.dto.card;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.bankcards.entity.enums.CardStatus;

public record CardReadDTO(UUID id, String number, LocalDate expirationDate, CardStatus status, Long balance,
                UUID userId, LocalDateTime createdAt, LocalDateTime updatedAt) {

}
