package com.example.bankcards.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
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
import com.example.bankcards.exception.handler.ErrorInfo;
import com.example.bankcards.service.CardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Tag(name = "Cards", description = "Operations with cards")
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@RestController
@RequestMapping(value = "/api/v1/cards", produces = "application/json")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardController {

    @Autowired
    CardService service;

    @Operation(summary = "Get card by id", description = "Returns card information by its identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = ErrorInfo.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(implementation = ErrorInfo.class)))
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public CardReadDTO getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @Operation(summary = "Get all cards by filter", description = "Returns all cards information by filter and pageable")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping()
    public PagedModel<CardReadDTO> getAllByFilter(@ParameterObject CardFilter filter,
            @ParameterObject @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return service.getAllByFilter(filter, pageable);
    }

    @Operation(summary = "Create card", description = "Creates a new card. Available for admin role only.")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CardReadDTO create(@RequestBody @Valid CardCreateDTO dto) {
        return service.create(dto);
    }

    @Operation(summary = "Update card status", description = "Updates card status. Available for admin role only.")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public CardReadDTO updateStatus(@PathVariable UUID id, @RequestBody @Valid CardUpdateStatusDTO dto) {
        return service.updateStatus(id, dto);
    }

    @Operation(summary = "Block card", description = "Blocks own card. Available for user role only.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CardReadDTO.class), 
        examples = @ExampleObject("""
            {
                "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                "number": "**** **** **** 2820",
                "expirationDate": "2025-12-23",
                "status": "BLOCKED",
                "balance": 0,
                "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                "createdAt": "2025-12-23T01:59:56.379Z",
                "updatedAt": "2025-12-23T01:59:56.379Z"
            }
            """)))
    @PreAuthorize("hasRole('USER')")
    @PatchMapping("/{id}/block")
    public CardReadDTO blockRequest(@PathVariable UUID id) {
        return service.blockRequest(id);
    }

    @Operation(summary = "Get all statuses", description = "Returns all statuses")
    @ApiResponse(responseCode = "200", description = "OK", 
        content = @Content(schema = @Schema(type = "object", properties = @StringToClassMapItem(key = "statuses", value = CardStatus[].class)),
            examples = @ExampleObject("""
                {
                    "statuses": [
                        "ACTIVE", "BLOCKED", "EXPIRED"
                    ]
                }
                """)))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statuses")
    public Map<String, List<CardStatus>> getAllStatuses() {
        return service.getAllStatuses();
    }

    @Operation(summary = "Transfer", description = "Transfer money between own cards. Available for user role only.")
    @ApiResponse(responseCode = "200", description = "OK", 
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = CardReadDTO.class), maxItems = 2, minItems = 2)))
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/transfer")
    public List<CardReadDTO> transfer(@RequestBody @Valid CardTransfer transfer) {
        return service.transfer(transfer);
    }

    @Operation(summary = "Delete card", description = "Deletes card. Available for admin role only.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        service.deleteById(id);
    }

}
