package com.example.bankcards.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankcards.dto.card.CardCreateDTO;
import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.dto.card.CardReadDTO;
import com.example.bankcards.dto.card.CardTransfer;
import com.example.bankcards.dto.card.CardUpdateStatusDTO;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.AuthenticationResolver;
import com.example.bankcards.security.UserRightValidator;
import com.example.bankcards.util.CardNumberUtil;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Service for managing cards
 */

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

    @Autowired
    AuthenticationResolver authenticationResolver;

    /**
     * <p>
     * Get card by id from database.
     * </p>
     * <p>
     * The card can be got by the owner or a user with the admin role.
     * </p>
     * 
     * @param id {@code UUID}
     * @return {@link CardEntity}
     * @throws EntityNotFoundException if card not found
     * @throws AccessDeniedException   if user has no right to get card
     */
    protected CardEntity getEntityById(UUID id) {
        var card = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Card not found with id=%s".formatted(id)));
        if (!userRightValidator.isUserRightGetCard(card.getUser().getId())) {
            throw new AccessDeniedException("You do not have access rights to the card id=%s".formatted(id));
        }
        return card;
    }

    /**
     * <p>
     * Get data transfer object represent card by id.
     * </p>
     * <p>
     * The card number is hidden by mask like <b>**** **** **** 2820</b>
     * </p>
     * 
     * @param id {@code UUID}
     * @return {@link CardReadDTO}
     * @throws EntityNotFoundException if card not found
     * @throws AccessDeniedException   if user has no right to get card
     */
    public CardReadDTO getById(UUID id) {
        return mapper.cardEntityToReadDTO(getEntityById(id));
    }

    /**
     * <p>
     * Get all cards by filter with pagination and sorting
     * </p>
     * <p>
     * For users with the admin role, all cards are available, while for others,
     * only their own cards are available.
     * </p>
     * 
     * @param filter   {@link CardFilter}
     * @param pageable {@link Pageable}
     * @return {@link PagedModel} of {@link CardReadDTO}
     */
    public PagedModel<CardReadDTO> getAllByFilter(CardFilter filter, Pageable pageable) {
        if (!authenticationResolver.userHasRole(RoleType.ADMIN)) {
            filter = new CardFilter(filter, authenticationResolver.getUser().getId());
        }
        return repository.findAllByFilter(filter, pageable);
    }

    /**
     * Create new card with auto generated number
     * 
     * @param dto {@link CardCreateDTO}
     * @return {@link CardReadDTO} created card
     */
    public CardReadDTO create(CardCreateDTO dto) {
        var user = userService.getEntityById(dto.userId());
        var number = generateCardNumber();
        var card = mapper.cardCreateToCardEntity(dto, user, number);
        return mapper.cardEntityToReadDTO(repository.save(card));
    }

    /**
     * Update card status without restrictions
     * 
     * @param id  {@code UUID}
     * @param dto {@link CardUpdateStatusDTO}
     * @return {@link CardReadDTO} with updated status
     * @throws EntityNotFoundException if card not found
     * @throws AccessDeniedException   if user has no right to get card
     */
    public CardReadDTO updateStatus(UUID id, CardUpdateStatusDTO dto) {
        var card = getEntityById(id);
        card.setStatus(dto.status());
        return mapper.cardEntityToReadDTO(repository.save(card));
    }

    /**
     * Card blocking request
     * 
     * @param id {@code UUID}
     * @return {@link CardReadDTO} with updated status
     * @throws EntityNotFoundException if card not found
     * @throws AccessDeniedException   if user has no right to get card
     */
    public CardReadDTO blockRequest(UUID id) {
        return updateStatus(id, new CardUpdateStatusDTO(CardStatus.BLOCKED));
    }

    /**
     * Balance transfer between user's cards
     * 
     * @param transfer {@link CardTransfer}
     * @return {@code List} of {@link CardReadDTO} between which balance was transferred
     * @throws EntityNotFoundException      if card not found
     * @throws AccessDeniedException        if user is not the owner of the card or
     *                                      cards are not active
     * @throws InsufficientBalanceException if card balance from which money is
     *                                      transferred is less than transfer amount
     */
    @Transactional(rollbackFor = Exception.class)
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

    /**
     * Delete card by id from database
     * 
     * @param id {@code UUID}
     * @throws EntityNotFoundException if card not found
     * @throws AccessDeniedException   if user has no right
     */
    public void deleteById(UUID id) {
        var card = getEntityById(id);
        repository.delete(card);
    }

    /**
     * Get all card's statuses
     * 
     * @return {@code Map<String, List<CardStatus>>} return JSON object like
     *         <i>{"statuses": ["ACTIVE", "BLOCKED", "EXPIRED"]}</i>
     */
    public Map<String, List<CardStatus>> getAllStatuses() {
        return Map.of("statuses", List.of(CardStatus.values()));
    }

    /**
     * Generate unused card number
     * 
     * @return {@code String} card number consists of 16 digits
     */
    private String generateCardNumber() {
        String number;
        do {
            number = CardNumberUtil.generate();
        } while (repository.existsByNumber(number));
        return number;
    }

}
