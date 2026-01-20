package com.lovingapp.loving.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import com.lovingapp.loving.model.dto.RitualFilterDTO;
import com.lovingapp.loving.model.entity.Ritual;
import com.lovingapp.loving.model.enums.PublicationStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class RitualRepositoryImpl implements RitualRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Ritual> search(RitualFilterDTO filter, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT r.* FROM rituals r WHERE 1=1 ");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM rituals r WHERE 1=1 ");

        Map<String, Object> params = new HashMap<>();
        List<String> where = new ArrayList<>();

        // JSONB array filters (enums stored as strings)
        if (!CollectionUtils.isEmpty(filter.getLoveTypes())) {
            where.add("EXISTS (SELECT 1 FROM jsonb_array_elements_text(r.love_types) v WHERE v IN (:loveTypes))");
            params.put("loveTypes", filter.getLoveTypes().stream().map(Enum::name).toList());
        }
        if (!CollectionUtils.isEmpty(filter.getRelationalNeeds())) {
            where.add(
                    "EXISTS (SELECT 1 FROM jsonb_array_elements_text(r.relational_needs) v WHERE v IN (:relationalNeeds))");
            params.put("relationalNeeds", filter.getRelationalNeeds().stream().map(Enum::name).toList());
        }
        if (!CollectionUtils.isEmpty(filter.getRitualTones())) {
            where.add(
                    "EXISTS (SELECT 1 FROM jsonb_array_elements_text(r.ritual_tones) v WHERE v IN (:ritualTones))");
            params.put("ritualTones", filter.getRitualTones().stream().map(Enum::name).toList());
        }

        // Simple enum column filter
        if (!CollectionUtils.isEmpty(filter.getRitualModes())) {
            where.add("r.ritual_mode IN (:ritualModes)");
            params.put("ritualModes", filter.getRitualModes().stream().map(Enum::name).toList());
        }
        if (!CollectionUtils.isEmpty(filter.getTimeTaken())) {
            where.add("r.time_taken IN (:timeTaken)");
            params.put("timeTaken", filter.getTimeTaken().stream().map(Enum::name).toList());
        }

        // Filter by status == PUBLISHED
        where.add("r.status = :status");
        params.put("status", PublicationStatus.PUBLISHED.name());

        if (!where.isEmpty()) {
            String whereClause = " AND " + String.join(" AND ", where) + " ";
            sql.append(whereClause);
            countSql.append(whereClause);
        }

        applySorting(sql, pageable);

        sql.append("OFFSET :offset LIMIT :limit");

        Query dataQuery = em.createNativeQuery(sql.toString(), Ritual.class);
        Query cntQuery = em.createNativeQuery(countSql.toString());

        // Bind parameters
        params.forEach((k, v) -> {
            dataQuery.setParameter(k, v);
            cntQuery.setParameter(k, v);
        });

        dataQuery.setParameter("offset", (int) pageable.getOffset());
        dataQuery.setParameter("limit", pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Ritual> content = dataQuery.getResultList();
        long total = ((Number) cntQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    private static final Map<String, String> SORT_COLUMN_MAP = Map.of(
            "createdAt", "r.created_at",
            "updatedAt", "r.updated_at",
            "title", "r.title");

    private static void applySorting(StringBuilder sql, Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            sql.append(" ORDER BY r.created_at DESC ");
            return;
        }

        Sort.Order order = pageable.getSort().iterator().next();
        String column = SORT_COLUMN_MAP.get(order.getProperty());

        if (column == null) {
            // Fallback to a safe default if an unsupported sort field is requested
            sql.append(" ORDER BY r.created_at DESC ");
            return;
        }

        sql.append(" ORDER BY ")
                .append(column)
                .append(order.isAscending() ? " ASC " : " DESC ");
    }
}
