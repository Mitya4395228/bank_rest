package com.example.bankcards.dto.card;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.bankcards.entity.enums.CardStatus;

public record CardFilter(LocalDate expirationDateFrom, LocalDate expirationDateTo, CardStatus status, Long minBalance,
                Long maxBalance, UUID userId, LocalDateTime createdFrom, LocalDateTime createdTo,
                LocalDateTime updatedFrom, LocalDateTime updatedTo) {

        public CardFilter(CardFilter filter, UUID userId) {
                this(filter.expirationDateFrom, filter.expirationDateTo, filter.status, filter.minBalance,
                                filter.maxBalance, userId, filter.createdFrom, filter.createdTo, filter.updatedFrom,
                                filter.updatedTo);
        }

}
