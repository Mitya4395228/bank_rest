package com.example.bankcards.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.bankcards.dto.card.CardCreateDTO;
import com.example.bankcards.dto.card.CardReadDTO;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.util.CardNumberUtil;

@Mapper(componentModel = "spring", imports = CardNumberUtil.class)
public interface CardMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "number", expression = "java(CardNumberUtil.mask(cardEntity.getNumber()))")
    CardReadDTO cardEntityToReadDTO(CardEntity cardEntity);

    default CardEntity cardCreateToCardEntity(CardCreateDTO cardCreateDTO, UserEntity userEntity, String number) {
        var cardEntity = new CardEntity();
        cardEntity.setNumber(number);
        cardEntity.setExpirationDate(cardCreateDTO.expirationDate());
        cardEntity.setStatus(CardStatus.ACTIVE);
        cardEntity.setBalance(0L);
        cardEntity.setUser(userEntity);
        return cardEntity;
    }

}
