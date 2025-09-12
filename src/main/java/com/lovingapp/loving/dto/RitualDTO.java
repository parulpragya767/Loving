package com.lovingapp.loving.dto;

import com.lovingapp.loving.model.Ritual;

import java.util.List;

public class RitualDTO {
    private Long id;
    private String title;
    private String description;
    private List<String> tags;

    public RitualDTO() {}

    public RitualDTO(Ritual ritual) {
        this.id = ritual.getId();
        this.title = ritual.getTitle();
        this.description = ritual.getDescription();
        this.tags = ritual.getTags();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
