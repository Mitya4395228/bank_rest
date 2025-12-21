package com.example.bankcards.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
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

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.EntityNotFoundException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Testcontainers
@SpringBootTest
class UserServiceTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.5");

    @DynamicPropertySource
    static void containersProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void testGetEntityById() {
        UserEntity expected = createUser();
        UserEntity actual = userService.getEntityById(expected.getId());
        Assertions.assertThat(actual).usingRecursiveComparison().ignoringFields("roles").isEqualTo(expected);
        Assertions.assertThat(actual.getRoles()).usingRecursiveComparison().ignoringFields("users")
                .isEqualTo(expected.getRoles());
    }

    @Test
    void testGetEntityById_EntityNotFoundException() {
        assertThrows(EntityNotFoundException.class, () -> userService.getEntityById(UUID.randomUUID()));
    }

    private UserEntity createUser() {
        var user = Instancio.of(UserEntity.class).ignore(Select.field("id")).create();
        user.setRoles(Set.of(roleRepository.findAll().get(0)));
        return userRepository.save(user);
    }

}
