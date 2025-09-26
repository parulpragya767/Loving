package com.lovingapp.loving.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lovingapp.loving.model.LoveTypeInfo;
import com.lovingapp.loving.model.Ritual;
import com.lovingapp.loving.repository.LoveTypeRepository;
import com.lovingapp.loving.repository.RitualRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@Profile("!test") // Don't run in tests
public class DataInitializer {

    private final LoveTypeRepository loveTypeRepository;
    private final RitualRepository ritualRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DataInitializer(LoveTypeRepository loveTypeRepository, 
                          RitualRepository ritualRepository,
                          ObjectMapper objectMapper) {
        this.loveTypeRepository = loveTypeRepository;
        this.ritualRepository = ritualRepository;
        this.objectMapper = objectMapper.copy()
            .registerModule(new JavaTimeModule());
    }

    @PostConstruct
    @Transactional
    public void init() {
        // Only initialize if the database is empty
        if (loveTypeRepository.count() == 0) {
            initializeLoveTypes();
        }
        if (ritualRepository.count() == 0) {
            initializeRituals();
        }
    }

    private void initializeLoveTypes() {
        // Only insert if no love types exist
        if (loveTypeRepository.count() == 0) {
            try {
                // Read JSON file from resources
                ClassPathResource resource = new ClassPathResource("data/loveTypes.json");
                String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

                // Deserialize JSON to List<LoveTypeInfo>
                List<LoveTypeInfo> loveTypes = objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, LoveTypeInfo.class)
                );

                // Save all love types
                loveTypeRepository.saveAll(loveTypes);
                
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize love types from JSON file", e);
            }
        }
    }

    private void initializeRituals() {
        // Only insert if no rituals exist
        if (ritualRepository.count() == 0) {
            try {
                // Read JSON file from resources
                ClassPathResource resource = new ClassPathResource("data/rituals.json");
                String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

                // Configure ObjectMapper to handle enums and Java 8 date/time
                ObjectMapper objectMapper = JsonMapper.builder()
                    .addModule(new JavaTimeModule())
                    .build();

                // Deserialize JSON to List<Ritual>
                List<Ritual> rituals = objectMapper.readValue(json, new TypeReference<List<Ritual>>() {});

                // Save all rituals
                ritualRepository.saveAll(rituals);
                
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize rituals from JSON file", e);
            }
        }
    }
    
}
