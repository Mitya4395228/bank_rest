package com.example.bankcards.entity;

import java.time.LocalDate;
import java.util.UUID;

import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.util.CardNumberUtil;
import com.example.bankcards.util.StringEncryptor;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "cards", schema = "finance")
public class CardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Nonnull
    @Pattern(regexp = CardNumberUtil.VALID_CARD_NUMBER, message = "The curd number must be 16 digits")
    @Convert(converter = StringEncryptor.class)
    String number;

    @Nonnull
    LocalDate expirationDate;

    @Nonnull
    @Enumerated(EnumType.STRING)
    CardStatus status;

    @Nonnull
    Long balance;

    @Nonnull
    @ManyToOne
    @JoinColumn(name = "user_id")
    UserEntity user;

}
