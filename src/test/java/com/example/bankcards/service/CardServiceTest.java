package com.example.bankcards.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.bankcards.dto.card.CardCreateDTO;
import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.dto.card.CardReadDTO;
import com.example.bankcards.dto.card.CardTransfer;
import com.example.bankcards.dto.card.CardUpdateStatusDTO;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.exception.InsufficientBalanceException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.AuthenticationResolver;
import com.example.bankcards.security.UserInfoDetails;
import com.example.bankcards.util.CardNumberUtil;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Testcontainers
@SpringBootTest
class CardServiceTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.5");

    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    CardRepository cardRepository;

    @Autowired
    CardService cardService;

    @MockitoBean
    AuthenticationResolver authenticationResolver;

    @BeforeEach
    void setUp() throws Exception {
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testGetEntityById() {
        CardEntity expected = createCard();
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(expected.getUser()));
        CardEntity actual = cardService.getEntityById(expected.getId());

        Assertions.assertThat(actual).usingRecursiveComparison(configRecursiveComparison()).ignoringFields("user")
                .isEqualTo(expected);
        assertEquals(expected.getUser().getId(), actual.getUser().getId());
    }

    @Test
    void testGetEntityById_EntityNotFoundException() {
        assertThrows(EntityNotFoundException.class, () -> cardService.getEntityById(UUID.randomUUID()));
    }

    @Test
    void testGetEntityById_AccessDeniedException() {
        CardEntity expected = createCard();
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(createUser(RoleType.USER)));
        assertThrows(AccessDeniedException.class, () -> cardService.getEntityById(expected.getId()));
    }

    @Test
    void testGetById() {
        CardEntity expected = createCard();
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(expected.getUser()));
        CardReadDTO actual = cardService.getById(expected.getId());

        Assertions.assertThat(expected).usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("user", "number").isEqualTo(actual);
        assertEquals(expected.getUser().getId(), actual.userId());
        assertTrue(expected.getNumber().endsWith(actual.number().replaceAll("[*]{4} ", "")));
    }

    @Test
    void testGetAllByFilter_ADMIN() {

        UserEntity admin = createUser(RoleType.ADMIN);
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(admin));

        UserEntity user = createUser();
        CardEntity card = createCard(user, CardStatus.ACTIVE);
        CardEntity cardNotFoundWithFullFilter = createCard(admin, CardStatus.BLOCKED);

        var role = "ROLE_" + RoleType.ADMIN;
        when(authenticationResolver.userHasRole(role)).thenReturn(true);

        // full filter
        CardFilter filter = new CardFilter(card.getExpirationDate(), card.getExpirationDate(), CardStatus.ACTIVE,
                card.getBalance(), card.getBalance(), user.getId(), card.getCreatedAt(), card.getCreatedAt(),
                card.getUpdatedAt(), card.getUpdatedAt());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("userId"));

        PagedModel<CardReadDTO> pageResult = cardService.getAllByFilter(filter, pageable);

        assertTrue(pageResult.getMetadata().number() == pageable.getPageNumber());
        assertTrue(pageResult.getMetadata().size() == pageable.getPageSize());
        assertTrue(pageResult.getMetadata().totalPages() == 1);
        assertTrue(pageResult.getMetadata().totalElements() == 1);
        assertTrue(pageResult.getContent().size() == 1);
        Assertions.assertThat(List.of(card)).usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("user", "number").isEqualTo(pageResult.getContent());
        assertEquals(pageResult.getContent().get(0).userId(), user.getId());
        assertNotEquals(cardNotFoundWithFullFilter.getId(), pageResult.getContent().get(0).id());

        // empty filter
        filter = new CardFilter(null, null, null, null, null, null, null, null, null, null);

        pageResult = cardService.getAllByFilter(filter, pageable);

        assertTrue(pageResult.getMetadata().number() == pageable.getPageNumber());
        assertTrue(pageResult.getMetadata().size() == pageable.getPageSize());
        assertTrue(pageResult.getMetadata().totalPages() == 1);
        assertTrue(pageResult.getMetadata().totalElements() == 2);
        assertTrue(pageResult.getContent().size() == 2);
        Assertions.assertThat(List.of(card, cardNotFoundWithFullFilter))
                .usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("user", "number").isEqualTo(pageResult.getContent());
        assertEquals(pageResult.getContent().get(0).userId(), user.getId());
        Assertions.assertThat(List.of(card.getUser().getId(), cardNotFoundWithFullFilter.getUser().getId()))
                .usingRecursiveComparison()
                .isEqualTo(pageResult.getContent().stream().map(CardReadDTO::userId).toList());

        verify(authenticationResolver, times(2)).userHasRole(role);
    }

    @Test
    void testGetAllByFilter_USER() {

        UserEntity userNotAuthorized = createUser();
        CardEntity cardNotFound = createCard(userNotAuthorized, CardStatus.ACTIVE);

        UserEntity user = createUser();
        CardEntity card = createCard(user, CardStatus.ACTIVE);

        var role = "ROLE_" + RoleType.ADMIN;
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(user));
        when(authenticationResolver.userHasRole(role)).thenReturn(false);

        // result only authorized user
        CardFilter filter = new CardFilter(null, null, null, null, null, userNotAuthorized.getId(), null, null, null,
                null);

        Pageable pageable = PageRequest.of(0, 5, Sort.by("userId"));

        PagedModel<CardReadDTO> pageResult = cardService.getAllByFilter(filter, pageable);

        assertTrue(pageResult.getMetadata().number() == pageable.getPageNumber());
        assertTrue(pageResult.getMetadata().size() == pageable.getPageSize());
        assertTrue(pageResult.getMetadata().totalPages() == 1);
        assertTrue(pageResult.getMetadata().totalElements() == 1);
        assertTrue(pageResult.getContent().size() == 1);
        assertEquals(pageResult.getContent().get(0).userId(), user.getId());
        Assertions.assertThat(List.of(card)).usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("user", "number").isEqualTo(pageResult.getContent());
        assertNotEquals(cardNotFound.getId(), pageResult.getContent().get(0).id());

        verify(authenticationResolver, times(1)).userHasRole(role);
        verify(authenticationResolver, times(1)).getUser();
    }

    @Test
    void testGetAllByUserId() {

        UserEntity userNotFound = createUser();
        CardEntity cardNotFound = createCard(userNotFound, CardStatus.ACTIVE);

        UserEntity user = createUser();
        CardEntity card1 = createCard(user, CardStatus.ACTIVE);
        CardEntity card2 = createCard(user, CardStatus.ACTIVE);

        UserEntity admin = createUser(RoleType.ADMIN);
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(admin));

        List<CardReadDTO> result = cardService.getAllByUserId(user.getId());

        assertTrue(result.size() == 2);
        Assertions.assertThat(List.of(card1, card2)).usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("user", "number").isEqualTo(result);
        Assertions.assertThat(List.of(user.getId(), user.getId())).usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("user", "number").isEqualTo(result.stream().map(CardReadDTO::userId).toList());
        assertNotEquals(cardNotFound.getId(), result.get(0).id());
    }

    @Test
    void testCreate() {

        UserEntity user = createUser();
        CardCreateDTO createDTO = new CardCreateDTO(LocalDate.now(), user.getId());

        CardReadDTO result = cardService.create(createDTO);

        assertEquals(createDTO.expirationDate(), result.expirationDate());
        assertEquals(createDTO.userId(), result.userId());
    }

    @Test
    void testUpdateStatus() {

        UserEntity admin = createUser(RoleType.ADMIN);
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(admin));

        CardEntity card = createCard(createUser(), CardStatus.ACTIVE);

        CardReadDTO result = cardService.updateStatus(card.getId(), new CardUpdateStatusDTO(CardStatus.BLOCKED));
        Assertions.assertThat(card).usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("user", "number", "status").isEqualTo(result);
        assertNotEquals(card.getStatus(), result.status());
        assertEquals(CardStatus.BLOCKED, result.status());
    }

    @Test
    void testBlockRequest() {

        UserEntity user = createUser();
        CardEntity card = createCard(user, CardStatus.ACTIVE);
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(user));

        CardReadDTO result = cardService.blockRequest(card.getId());
        Assertions.assertThat(card).usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("user", "number", "status").isEqualTo(result);
        assertNotEquals(card.getStatus(), result.status());
        assertEquals(CardStatus.BLOCKED, result.status());
    }

    @Test
    void testTransfer() {

        UserEntity user = createUser();
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(user));

        CardEntity cardFrom = createCard(user, CardStatus.ACTIVE);
        CardEntity cardTo = createCard(user, CardStatus.ACTIVE);
        CardTransfer transfer = new CardTransfer(cardFrom.getId(), cardTo.getId(), 1L);

        List<CardReadDTO> result = cardService.transfer(transfer);
        Assertions.assertThat(List.of(cardFrom, cardTo)).usingRecursiveComparison(configRecursiveComparison())
                .ignoringFields("user", "number", "balance").isEqualTo(result);
        result.forEach(card -> {
            if (card.id().equals(cardFrom.getId())) {
                assertTrue(cardFrom.getBalance() - card.balance() == 1L);
            } else {
                assertTrue(card.balance() - cardTo.getBalance() == 1L);
            }
        });
    }

    @Test
    void testTransfer_InsufficientBalanceException() {

        UserEntity user = createUser();
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(user));

        CardEntity cardFrom = createCard(user, CardStatus.ACTIVE);
        CardEntity cardTo = createCard(user, CardStatus.ACTIVE);
        CardTransfer transfer = new CardTransfer(cardFrom.getId(), cardTo.getId(), cardFrom.getBalance() + 1L);

        assertThrows(InsufficientBalanceException.class, () -> cardService.transfer(transfer));
    }

    @Test
    void testTransfer_AccessDeniedException() {

        UserEntity user = createUser();
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(user));

        CardEntity cardFrom = createCard(user, CardStatus.ACTIVE);
        CardEntity cardTo = createCard(user, CardStatus.BLOCKED);
        CardTransfer transfer = new CardTransfer(cardFrom.getId(), cardTo.getId(), 1L);

        assertThrows(AccessDeniedException.class, () -> cardService.transfer(transfer));
    }

    @Test
    void testDeleteById() {
        UserEntity user = createUser();
        CardEntity card = createCard(user, CardStatus.ACTIVE);
        when(authenticationResolver.getUser()).thenReturn(new UserInfoDetails(user));

        CardEntity willDeleteCard = cardService.getEntityById(card.getId());
        cardService.deleteById(willDeleteCard.getId());
        assertThrows(EntityNotFoundException.class, () -> cardService.getEntityById(willDeleteCard.getId()));
    }

    @Test
    void testGetAllStatuses() {
        Map<String, List<CardStatus>> statuses = cardService.getAllStatuses();
        assertTrue(statuses.containsKey("statuses"));
        assertTrue(statuses.size() == 1);
        Assertions.assertThat(statuses.get("statuses")).usingRecursiveComparison()
                .isEqualTo(List.of(CardStatus.values()));
    }

    private UserEntity createUser() {
        return createUser(RoleType.USER);
    }

    private UserEntity createUser(RoleType role) {
        UserEntity user = Instancio.of(UserEntity.class).ignore(Select.field("id")).create();
        user.setRoles(Set.of(roleRepository.findAll().stream().filter(f -> f.getRole() == role).findFirst().get()));
        return userRepository.save(user);
    }

    private CardEntity createCard() {
        return createCard(createUser(), CardStatus.ACTIVE);
    }

    private CardEntity createCard(UserEntity user, CardStatus status) {
        CardEntity card = Instancio.of(CardEntity.class).ignore(Select.field("id")).ignore(Select.field("user"))
                .ignore(Select.field("status"))
                .generate(Select.field("balance"), gen -> gen.longs().min(5L))
                .ignore(Select.field("number"))
                .create();
        card.setUser(user);
        card.setStatus(status);
        card.setNumber(CardNumberUtil.generate());
        return cardRepository.save(card);
    }

    private RecursiveComparisonConfiguration configRecursiveComparison() {
        return RecursiveComparisonConfiguration.builder()
                .withEqualsForType(
                        (t1, t2) -> t1.truncatedTo(ChronoUnit.SECONDS).equals(t2.truncatedTo(ChronoUnit.SECONDS)),
                        LocalDateTime.class)
                .build();
    }

}
