package com.lovingapp.loving.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lovingapp.loving.model.LoveType;
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
import java.util.Arrays;
import java.util.List;

@Configuration
@Profile("!test") // Don't run in tests
public class DataInitializer {

    private final LoveTypeRepository loveTypeRepository;
    private final RitualRepository ritualRepository;

    @Autowired
    public DataInitializer(LoveTypeRepository loveTypeRepository, 
                          RitualRepository ritualRepository) {
        this.loveTypeRepository = loveTypeRepository;
        this.ritualRepository = ritualRepository;
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
            List<LoveType> loveTypes = Arrays.asList(
                new LoveType("Romantic Love", "Deep emotional connection and passion"),
                new LoveType("Platonic Love", "Non-romantic affection between friends"),
                new LoveType("Familial Love", "Bond between family members"),
                new LoveType("Self-Love", "Appreciation and care for oneself"),
                new LoveType("Companionate Love", "Deep friendship and long-term commitment")
            );
            loveTypeRepository.saveAll(loveTypes);
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
