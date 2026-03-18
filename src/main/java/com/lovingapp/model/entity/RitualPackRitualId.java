package com.lovingapp.model.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class RitualPackRitualId implements Serializable {

    @Column(name = "ritual_pack_id")
    private UUID ritualPackId;

    @Column(name = "ritual_id")
    private UUID ritualId;
}
