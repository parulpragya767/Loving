package com.lovingapp.loving.model.entity;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.lovingapp.loving.model.enums.LoveType;
import com.vladmihalcea.hibernate.type.json.JsonType;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Column(nullable = false, updatable = false)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer id;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @Enumerated(EnumType.STRING)
    @Column(name = "love_type", length = 20, nullable = false, unique = true)
    private LoveType loveType;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(nullable = false, length = 30)
    private String title;

    @Column(nullable = false, length = 255)
    private String subtitle;

    @Column(columnDefinition = "text")
    private String description; // markdown supported

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    private List<InfoSection> sections = new ArrayList<>();

    @Column(name = "content_hash", length = 64, nullable = false)
    private String contentHash;

    @CreationTimestamp
    @Column(name = "created_at", columnDefinition = "timestamptz", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamptz", nullable = false)
    private OffsetDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InfoSection {
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private int order;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String title;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String summary;

        private List<InfoBullet> bullets;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InfoSection that = (InfoSection) o;
            return order == that.order && Objects.equals(title, that.title) && Objects.equals(summary, that.summary)
                    && Objects.equals(bullets, that.bullets);
        }

        @Override
        public int hashCode() {
            return Objects.hash(order, title, summary, bullets);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InfoBullet {
        private String title; // optional

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        private String text; // markdown supported

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InfoBullet that = (InfoBullet) o;
            return Objects.equals(title, that.title) && Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, text);
        }
    }
}
