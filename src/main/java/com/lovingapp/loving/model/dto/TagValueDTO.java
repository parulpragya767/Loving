package com.lovingapp.loving.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagValueDTO {
    private String key;
    private String displayName;
}