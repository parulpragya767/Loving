package com.lovingapp.loving.model.entity;

import java.util.List;

import org.hibernate.annotations.Type;

import com.lovingapp.loving.model.enums.LoveType;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "love_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoveTypeInfo {

    @Id
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "love_type", length = 20, nullable = false)
    private LoveType loveType;

    @Column(nullable = false)
    private String title;

    @Column
    private String subtitle;

    @Column(columnDefinition = "text")
    private String description; // markdown supported

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<InfoSection> sections;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InfoSection {
        private int order;
        private String title;
        private String summary;
        private List<InfoBullet> bullets;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InfoBullet {
        private String title; // optional
        private String text; // markdown supported
    }
}
