package com.lovingapp.loving.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import com.lovingapp.loving.model.enums.LoveType;
import com.vladmihalcea.hibernate.type.json.JsonType;

import java.util.List;

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
        private String text;  // markdown supported
    }
}

