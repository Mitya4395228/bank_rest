package com.example.bankcards.dto.card;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public record CardCreateDTO(@NotNull @Future LocalDate expirationDate, @NotNull UUID userId) {

}
