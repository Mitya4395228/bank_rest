package com.example.bankcards.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.dto.card.CardReadDTO;

public interface CardRepositoryCustom {

    PagedModel<CardReadDTO> findAllByFilter(CardFilter filter, Pageable pageable);

}
