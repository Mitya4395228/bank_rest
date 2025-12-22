package com.example.bankcards.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import com.example.bankcards.dto.card.CardFilter;
import com.example.bankcards.dto.card.CardReadDTO;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.mapper.CardMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class CardRepositoryCustomImpl implements CardRepositoryCustom {

    @PersistenceContext
    EntityManager em;

    @Autowired
    CardMapper mapper;

    @Override
    public PagedModel<CardReadDTO> findAllByFilter(CardFilter filter, Pageable pageable) {

        var countString = "SELECT COUNT(c) FROM CardEntity c WHERE 1=1";
        var queryString = "SELECT c FROM CardEntity c WHERE 1=1";
        var qlString = new StringBuilder();
        var parameters = new HashMap<String, Object>();

        if (filter.expirationDateFrom() != null) {
            qlString.append(" AND c.expirationDate >= :expirationDateFrom");
            parameters.put("expirationDateFrom", filter.expirationDateFrom());
        }
        if (filter.expirationDateTo() != null) {
            qlString.append(" AND c.expirationDate <= :expirationDateTo");
            parameters.put("expirationDateTo", filter.expirationDateTo());
        }
        if (filter.status() != null) {
            qlString.append(" AND c.status = :status");
            parameters.put("status", filter.status());
        }
        if (filter.minBalance() != null) {
            qlString.append(" AND c.balance >= :minBalance");
            parameters.put("minBalance", filter.minBalance());
        }
        if (filter.maxBalance() != null) {
            qlString.append(" AND c.balance <= :maxBalance");
            parameters.put("maxBalance", filter.maxBalance());
        }
        if (filter.userId() != null) {
            qlString.append(" AND c.user.id = :userId");
            parameters.put("userId", filter.userId());
        }
        if (filter.createdFrom() != null) {
            qlString.append(" AND c.createdAt >= :createdFrom");
            parameters.put("createdFrom", filter.createdFrom());
        }
        if (filter.createdTo() != null) {
            qlString.append(" AND c.createdAt <= :createdTo");
            parameters.put("createdTo", filter.createdTo());
        }
        if (filter.updatedFrom() != null) {
            qlString.append(" AND c.updatedAt >= :updatedFrom");
            parameters.put("updatedFrom", filter.updatedFrom());
        }
        if (filter.updatedTo() != null) {
            qlString.append(" AND c.updatedAt <= :updatedTo");
            parameters.put("updatedTo", filter.updatedTo());
        }

        var totalElements = count(countString + qlString.toString(), parameters);

        if (pageable.getSort() != null && !pageable.getSort().isEmpty()) {
            qlString.append(" ORDER BY " + pageable.getSort().stream()
                    .map(order -> order.getProperty().equals("userId") ? "c.user.id %s".formatted(order.getDirection())
                            : "c.%s %s".formatted(order.getProperty(), order.getDirection()))
                    .collect(Collectors.joining(", ")));
        }

        var query = em.createQuery(queryString + qlString.toString(), CardEntity.class);
        parameters.forEach(query::setParameter);

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize());

        var content = query.getResultList().stream().map(mapper::cardEntityToReadDTO).toList();
        
       return new PagedModel<CardReadDTO>(new PageImpl<CardReadDTO>(content, pageable, totalElements));
    }

    private long count(String qlString, Map<String, Object> parameters) {
        var query = em.createQuery(qlString.toString());
        parameters.forEach(query::setParameter);
        return (long) query.getSingleResult();
    }

}
