package com.lovingapp.loving.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.lovingapp.loving.model.dto.RitualFilterRequest;
import com.lovingapp.loving.model.entity.Ritual;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;

public class RitualRepositoryImpl implements RitualRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Page<Ritual> search(RitualFilterRequest filter, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM rituals r WHERE 1=1 ");

        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(*) FROM rituals r WHERE 1=1 ");

        Map<String, Object> params = new HashMap<>();
        List<String> where = new ArrayList<>();

        // JSONB array filters (enums stored as strings)
        if (filter.getLoveTypes() != null && !filter.getLoveTypes().isEmpty()) {
            where.add("EXISTS (SELECT 1 FROM jsonb_array_elements_text(r.love_types) v WHERE v IN (:loveTypes))");
            params.put("loveTypes", filter.getLoveTypes().stream().map(Enum::name).toList());
        }
        if (filter.getRelationalNeeds() != null && !filter.getRelationalNeeds().isEmpty()) {
            where.add(
                    "EXISTS (SELECT 1 FROM jsonb_array_elements_text(r.relational_needs) v WHERE v IN (:relationalNeeds))");
            params.put("relationalNeeds", filter.getRelationalNeeds().stream().map(Enum::name).toList());
        }

        // Simple enum column filter
        if (filter.getRitualModes() != null && !filter.getRitualModes().isEmpty()) {
            where.add("r.ritual_mode IN (:ritualModes)");
            params.put("ritualModes", filter.getRitualModes().stream().map(Enum::name).toList());
        }

        // Filter by status if provided
        if (filter.getStatus() != null) {
            where.add("r.status = :status");
            params.put("status", filter.getStatus().name());
        }

        if (!where.isEmpty()) {
            String whereClause = " AND " + String.join(" AND ", where) + " ";
            sql.append(whereClause);
            countSql.append(whereClause);
        }

        // Default ordering; pageable sort mapping can be added as needed
        sql.append("ORDER BY r.created_at DESC ");
        sql.append("OFFSET :offset LIMIT :limit");

        Query dataQuery = em.createNativeQuery(sql.toString(), Ritual.class);
        Query cntQuery = em.createNativeQuery(countSql.toString());

        // Bind parameters
        for (Map.Entry<String, Object> e : params.entrySet()) {
            dataQuery.setParameter(e.getKey(), e.getValue());
            cntQuery.setParameter(e.getKey(), e.getValue());
        }
        dataQuery.setParameter("offset", (int) pageable.getOffset());
        dataQuery.setParameter("limit", pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Ritual> content = dataQuery.getResultList();
        Number total = ((Number) cntQuery.getSingleResult());

        return new PageImpl<>(content, pageable, total.longValue());
    }
}
