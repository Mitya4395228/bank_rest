package com.example.bankcards.job;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.RoleType;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberUtil;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Testcontainers
@SpringBootTest
class CardTasksTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.5");

    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("update.expired-cards.job.cron",
                () -> LocalTime.now().plusSeconds(5).format(DateTimeFormatter.ofPattern("ss mm HH * * ?")));
    }

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    CardRepository cardRepository;

    @BeforeEach
    void setUp() throws Exception {
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testJobSetExpired() throws InterruptedException {

        CardEntity expiredCard = createCard(LocalDate.now().minusDays(1));
        CardEntity activeCard = createCard(LocalDate.now().plusDays(1));

        Thread.sleep(6000);

        CardEntity resultExpiredCard = cardRepository.findById(expiredCard.getId()).orElse(null);
        CardEntity resultActiveCard = cardRepository.findById(activeCard.getId()).orElse(null);

        assertNotNull(resultExpiredCard);
        assertEquals(CardStatus.EXPIRED, resultExpiredCard.getStatus());
        assertNotNull(resultActiveCard);
        assertEquals(CardStatus.ACTIVE, resultActiveCard.getStatus());

    }

    private UserEntity createUser() {
        UserEntity user = Instancio.of(UserEntity.class).ignore(Select.field("id")).create();
        user.setRoles(
                Set.of(roleRepository.findAll()
                        .stream()
                        .filter(f -> f.getRole().equals(RoleType.USER))
                        .findFirst()
                        .get()));
        return userRepository.save(user);
    }

    private CardEntity createCard(LocalDate expirationDate) {
        CardEntity card = Instancio.of(CardEntity.class).ignore(Select.field("id")).ignore(Select.field("user"))
                .ignore(Select.field("status"))
                .generate(Select.field("balance"), gen -> gen.longs().min(5L))
                .ignore(Select.field("number"))
                .ignore(Select.field("expirationDate"))
                .create();
        card.setUser(createUser());
        card.setStatus(CardStatus.ACTIVE);
        card.setNumber(CardNumberUtil.generate());
        card.setExpirationDate(expirationDate);
        return cardRepository.save(card);
    }

}
