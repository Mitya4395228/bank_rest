package com.example.bankcards.dto.card;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.bankcards.entity.enums.CardStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Card read DTO")
public record CardReadDTO(UUID id, @Schema(description = "Card number", example = "**** **** **** 2820") String number,
        LocalDate expirationDate, CardStatus status, Long balance, UUID userId, LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
