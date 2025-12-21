package com.example.bankcards.controller;

import static org.junit.jupiter.api.Assertions.*;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.bankcards.dto.auth.AuthRequest;
import com.example.bankcards.dto.auth.AuthResponse;
import com.example.bankcards.security.AuthService;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.UserInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = AuthController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    JwtService jwtService;

    @MockitoBean
    UserInfoService infoService;

    static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testLogin() throws Exception {

        var authResponse = Instancio.create(AuthResponse.class);
        var authRequest = Instancio.create(AuthRequest.class);

        when(authService.generateToken(authRequest)).thenReturn(authResponse);

        String resultJson = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)).with(csrf()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var result = objectMapper.readValue(resultJson, AuthResponse.class);

        assertEquals(authResponse, result);
        verify(authService, times(1)).generateToken(authRequest);
    }

    @Test
    void testLogin_Validation() throws Exception {
        var authRequest = new AuthRequest("", null);
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)).with(csrf()))
                .andExpect(status().isBadRequest());
        verify(authService, never()).generateToken(any(AuthRequest.class));
    }

}