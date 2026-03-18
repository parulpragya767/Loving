package com.lovingapp.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ritual_pack_rituals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RitualPackRitual {

    @EmbeddedId
    private RitualPackRitualId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ritualPackId")
    @JoinColumn(name = "ritual_pack_id")
    private RitualPack ritualPack;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ritualId")
    @JoinColumn(name = "ritual_id")
    private Ritual ritual;

    @Column(name = "position", nullable = false)
    private int position;
}
