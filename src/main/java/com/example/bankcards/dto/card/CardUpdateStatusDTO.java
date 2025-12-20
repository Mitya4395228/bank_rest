package com.example.bankcards.dto.card;

import com.example.bankcards.entity.enums.CardStatus;

import jakarta.validation.constraints.NotNull;

public record CardUpdateStatusDTO(@NotNull CardStatus status) {

}
