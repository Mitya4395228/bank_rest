package com.example.bankcards.dto.card;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CardTransfer(@NotNull UUID fromCard, @NotNull UUID toCard, @NotNull @Positive Long amount) {

}
