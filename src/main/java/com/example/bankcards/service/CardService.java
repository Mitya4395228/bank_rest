package com.example.bankcards.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.bankcards.dto.card.CardCreateDTO;
import com.example.bankcards.dto.card.CardReadDTO;
import com.example.bankcards.dto.card.CardTransfer;
import com.example.bankcards.dto.card.CardUpdateStatusDTO;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.UserRightValidator;
import com.example.bankcards.util.CardNumberUtil;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Service
public class CardService {

    @Autowired
    CardRepository repository;

    @Autowired
    UserService userService;

    @Autowired
    CardMapper mapper;

    @Autowired
    UserRightValidator userRightValidator;

    public CardEntity getEntityById(UUID id) {
        var card = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id=%s".formatted(id)));
        if (!userRightValidator.isUserRightGetCard(card.getUser().getId())) {
            throw new AccessDeniedException("You do not have access rights to the card id=%s".formatted(id));
        }
        return card;
    }

    public CardReadDTO getById(UUID id) {
        return mapper.cardEntityToReadDTO(getEntityById(id));
    }

    public List<CardReadDTO> getAllByUserId(UUID userId) {
        return repository.findAllByUserId(userId).stream().map(mapper::cardEntityToReadDTO).toList();
    }

    public CardReadDTO create(CardCreateDTO dto) {
        var user = userService.getEntityById(dto.userId());
        var number = generateCardNumber();
        var card = mapper.cardCreateToCardEntity(dto, user, number);
        return mapper.cardEntityToReadDTO(repository.save(card));
    }

    public CardReadDTO updateStatus(UUID id, CardUpdateStatusDTO dto) {
        var card = getEntityById(id);
        card.setStatus(dto.status());
        return mapper.cardEntityToReadDTO(repository.save(card));
    }

    public CardReadDTO blockRequest(UUID id) {
        return updateStatus(id, new CardUpdateStatusDTO(CardStatus.BLOCKED));
    }

    @Transactional(rollbackOn = Exception.class)
    public List<CardReadDTO> transfer(CardTransfer transfer) {

        var fromCard = getEntityById(transfer.fromCard());
        if (fromCard.getBalance() < transfer.amount()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance on card (id=%s) for transfer, balance=%d < transfer=%d"
                            .formatted(transfer.fromCard(), fromCard.getBalance(), transfer.amount()));
        }
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new AccessDeniedException("Card (id=%s) is not active".formatted(transfer.fromCard()));
        }
        fromCard.setBalance(fromCard.getBalance() - transfer.amount());

        var toCard = getEntityById(transfer.toCard());
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new AccessDeniedException("Card (id=%s) is not active".formatted(transfer.toCard()));
        }
        toCard.setBalance(toCard.getBalance() + transfer.amount());

        return repository.saveAll(List.of(fromCard, toCard)).stream().map(mapper::cardEntityToReadDTO).toList();
    }

    public void deleteById(UUID id) {
        var card = getEntityById(id);
        repository.delete(card);
    }

    public Map<String, List<CardStatus>> getAllStatuses() {
        return Map.of("statuses", List.of(CardStatus.values()));
    }

    private String generateCardNumber() {
        String number;
        do {
            number = CardNumberUtil.generate();
        } while (repository.existsByNumber(number));
        return number;
    }

}
