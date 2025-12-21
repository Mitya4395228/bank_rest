package com.example.bankcards.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.card.CardCreateDTO;
import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.dto.card.CardReadDTO;
import com.example.bankcards.dto.card.CardTransfer;
import com.example.bankcards.dto.card.CardUpdateStatusDTO;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/cards")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardController {

    @Autowired
    CardService service;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public CardReadDTO getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping()
    public PagedModel<CardReadDTO> getAllByFilter(CardFilter filter,
            @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return service.getAllByFilter(filter, pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CardReadDTO create(@RequestBody @Valid CardCreateDTO dto) {
        return service.create(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public CardReadDTO updateStatus(@PathVariable UUID id, @RequestBody @Valid CardUpdateStatusDTO dto) {
        return service.updateStatus(id, dto);
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}/block")
    public CardReadDTO blockRequest(@PathVariable UUID id) {
        return service.blockRequest(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statuses")
    public Map<String, List<CardStatus>> getAllStatuses() {
        return service.getAllStatuses();
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public List<CardReadDTO> transfer(@RequestBody @Valid CardTransfer transfer) {
        return service.transfer(transfer);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        service.deleteById(id);
    }

}
