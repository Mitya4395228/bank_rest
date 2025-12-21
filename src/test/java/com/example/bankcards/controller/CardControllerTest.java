package com.example.bankcards.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.instancio.Select;

import com.example.bankcards.dto.card.CardCreateDTO;
import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.dto.card.CardReadDTO;
import com.example.bankcards.dto.card.CardTransfer;
import com.example.bankcards.dto.card.CardUpdateStatusDTO;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.UserInfoService;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@WebMvcTest(controllers = CardController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class CardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CardService service;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    UserInfoService infoService;

    static final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Test
    @WithMockUser
    void testGetById_Success() throws Exception {

        CardReadDTO response = Instancio.create(CardReadDTO.class);
        UUID id = response.id();

        when(service.getById(id)).thenReturn(response);

        String resultJson = mockMvc.perform(get("/api/v1/cards/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CardReadDTO result = objectMapper.readValue(resultJson, CardReadDTO.class);
        assertEquals(response, result);
        verify(service, times(1)).getById(id);
    }

    @Test
    void testGetById_CannotAccess_Anonymous() throws Exception {
        mockMvc.perform(get("/api/v1/cards/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
        verify(service, never()).getById(any());
    }

    @Test
    @WithMockUser
    void getAllByFilter_Success() throws Exception {

        CardFilter filter = Instancio.create(CardFilter.class);
        Pageable pageable = PageRequest.of(0, 10);
        List<CardReadDTO> content = Instancio.ofList(CardReadDTO.class).size(3).create();
        PagedModel<CardReadDTO> response = new PagedModel<>(new PageImpl<>(content, pageable, content.size()));

        when(service.getAllByFilter(eq(filter), any(Pageable.class))).thenReturn(response);

        String resultJson = mockMvc.perform(
                get("/api/v1/cards")
                        .param("expirationDateFrom",
                                filter.expirationDateFrom() != null ? filter.expirationDateFrom().toString() : "")
                        .param("expirationDateTo",
                                filter.expirationDateTo() != null ? filter.expirationDateTo().toString() : "")
                        .param("status", filter.status() != null ? filter.status().name() : "")
                        .param("minBalance", filter.minBalance() != null ? filter.minBalance().toString() : "")
                        .param("maxBalance", filter.maxBalance() != null ? filter.maxBalance().toString() : "")
                        .param("userId", filter.userId() != null ? filter.userId().toString() : "")
                        .param("createdFrom", filter.createdFrom() != null ? filter.createdFrom().toString() : "")
                        .param("createdTo", filter.createdTo() != null ? filter.createdTo().toString() : "")
                        .param("updatedFrom", filter.updatedFrom() != null ? filter.updatedFrom().toString() : "")
                        .param("updatedTo", filter.updatedTo() != null ? filter.updatedTo().toString() : "")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> result = objectMapper.readValue(resultJson, Map.class);
        List<CardReadDTO> resultContent = objectMapper.convertValue(result.get("content"),
                new TypeReference<List<CardReadDTO>>() {
                });

        Assertions.assertThat(resultContent).isEqualTo(content);
        verify(service, times(1)).getAllByFilter(any(CardFilter.class), any(Pageable.class));
    }

    @Test
    void getAllByFilter_anonymous_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/cards"))
                .andExpect(status().isUnauthorized());

        verify(service, never()).getAllByFilter(any(), any());
    }

    @Test
    @WithMockUser
    void testCreate_Success() throws Exception {

        CardCreateDTO request = Instancio.of(CardCreateDTO.class)
                .generate(Select.field("expirationDate"), gen -> gen.temporal().localDate().future())
                .create();
        CardReadDTO response = Instancio.create(CardReadDTO.class);

        when(service.create(request)).thenReturn(response);

        String resultJson = mockMvc.perform(post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CardReadDTO result = objectMapper.readValue(resultJson, CardReadDTO.class);
        assertEquals(response, result);
        verify(service, times(1)).create(request);
    }

    @Test
    void testCreate_Anonymous() throws Exception {

        CardCreateDTO request = Instancio.of(CardCreateDTO.class)
                .generate(Select.field("expirationDate"), gen -> gen.temporal().localDate().future())
                .create();

        mockMvc.perform(post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(service, never()).create(any());
    }

    @Test
    @WithMockUser
    void testCreate_Validation() throws Exception {

        CardCreateDTO request = Instancio.of(CardCreateDTO.class)
                .generate(Select.field("expirationDate"), gen -> gen.temporal().localDate().past())
                .create();

        mockMvc.perform(post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    @WithMockUser
    void testUpdateStatus_Success() throws Exception {

        CardUpdateStatusDTO request = Instancio.create(CardUpdateStatusDTO.class);
        UUID id = UUID.randomUUID();
        CardReadDTO response = Instancio.create(CardReadDTO.class);

        when(service.updateStatus(id, request)).thenReturn(response);

        String resultJson = mockMvc.perform(patch("/api/v1/cards/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CardReadDTO result = objectMapper.readValue(resultJson, CardReadDTO.class);
        assertEquals(response, result);
        verify(service, times(1)).updateStatus(id, request);
    }

    @Test
    void testUpdateStatus_Anonymous() throws Exception {

        CardUpdateStatusDTO request = Instancio.create(CardUpdateStatusDTO.class);
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/cards/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(service, never()).updateStatus(id, request);
    }

    @Test
    @WithMockUser
    void testUpdateStatus_Validation() throws Exception {

        CardUpdateStatusDTO request = new CardUpdateStatusDTO(null);
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/cards/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(service, never()).updateStatus(id, request);
    }

    @Test
    @WithMockUser
    void testBlockRequest_Success() throws Exception {

        UUID id = UUID.randomUUID();
        CardReadDTO response = Instancio.create(CardReadDTO.class);

        when(service.blockRequest(id)).thenReturn(response);

        String resultJson = mockMvc.perform(patch("/api/v1/cards/{id}/block", id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CardReadDTO result = objectMapper.readValue(resultJson, CardReadDTO.class);
        assertEquals(response, result);
        verify(service, times(1)).blockRequest(id);
    }

    @Test
    void testBlockRequest_Anonymous() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/cards/{id}/block", id)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(service, never()).blockRequest(id);
    }

    @Test
    @WithMockUser
    void testGetAllStatuses_Success() throws Exception {

        Map<String, List<CardStatus>> response = Map.of("status", List.of(CardStatus.values()));

        when(service.getAllStatuses()).thenReturn(response);

        String resultJson = mockMvc.perform(get("/api/v1/cards/statuses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, List<CardStatus>> result = objectMapper.readValue(resultJson,
                new TypeReference<Map<String, List<CardStatus>>>() {
                });

        assertTrue(result.size() == 1);
        assertTrue(result.keySet().containsAll(response.keySet()));
        assertTrue(result.values().containsAll(response.values()));

        verify(service, times(1)).getAllStatuses();
    }

    @Test
    void testGetAllStatuses_Anonymous() throws Exception {

        mockMvc.perform(get("/api/v1/cards/statuses")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(service, never()).getAllStatuses();
    }

    @Test
    @WithMockUser
    void testTransfer_Success() throws Exception {

        CardTransfer request = Instancio.create(CardTransfer.class);
        List<CardReadDTO> response = Instancio.ofList(CardReadDTO.class).size(1).create();

        when(service.transfer(request)).thenReturn(response);

        String resultJson = mockMvc.perform(post("/api/v1/cards/transfer")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        List<CardReadDTO> result = objectMapper.readValue(resultJson, new TypeReference<List<CardReadDTO>>() {
        });

        Assertions.assertThat(result).usingRecursiveComparison().ignoringCollectionOrder().isEqualTo(response);
        verify(service, times(1)).transfer(request);
    }

    @Test
    void testTransfer_Anonymous() throws Exception {

        CardTransfer request = Instancio.create(CardTransfer.class);

        mockMvc.perform(post("/api/v1/cards/transfer")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(service, never()).transfer(request);
    }

    @Test
    void testTransfer_Validation() throws Exception {

        CardTransfer request = new CardTransfer(UUID.randomUUID(), UUID.randomUUID(), -1L);

        mockMvc.perform(post("/api/v1/cards/transfer")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(service, never()).transfer(request);
    }

    @Test
    @WithMockUser
    void testDleteById_Success() throws Exception {

        UUID id = UUID.randomUUID();
        doNothing().when(service).deleteById(id);

        mockMvc.perform(delete("/api/v1/cards/{id}", id).with(csrf()))
                .andExpect(status().isOk());

        verify(service, times(1)).deleteById(id);
    }

    @Test
    void testDleteById_Anonymous() throws Exception {

        mockMvc.perform(delete("/api/v1/cards/{id}", UUID.randomUUID()).with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(service, never()).deleteById(any());
    }

}
