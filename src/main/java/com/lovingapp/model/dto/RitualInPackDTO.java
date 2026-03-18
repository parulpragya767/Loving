package com.lovingapp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RitualInPackDTO {
    
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private RitualDTO ritual;
    
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private int position;
}
