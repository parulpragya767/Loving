package com.lovingapp.loving.model.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RitualTagDTO {
    private String displayName;
    private int position;
    private List<TagValueDTO> values;
}