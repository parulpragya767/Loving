package com.lovingapp.loving.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lovingapp.loving.model.LoveTypeInfo;
import com.lovingapp.loving.model.Ritual;
import com.lovingapp.loving.model.RitualPack;
import com.lovingapp.loving.model.UserContext;
import com.lovingapp.loving.repository.LoveTypeRepository;
import com.lovingapp.loving.repository.RitualRepository;
import com.lovingapp.loving.repository.RitualPackRepository;
import com.lovingapp.loving.repository.UserContextRepository;
import java.time.OffsetDateTime;

import jakarta.transaction.Transactional;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!test") // Don't run in tests
public class DataInitializer {

    private final LoveTypeRepository loveTypeRepository;
    private final RitualRepository ritualRepository;
    private final RitualPackRepository ritualPackRepository;
    private final UserContextRepository userContextRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DataInitializer(LoveTypeRepository loveTypeRepository, 
                          RitualRepository ritualRepository,
                          RitualPackRepository ritualPackRepository,
                          UserContextRepository userContextRepository,
                          ObjectMapper objectMapper) {
        this.loveTypeRepository = loveTypeRepository;
        this.ritualRepository = ritualRepository;
        this.ritualPackRepository = ritualPackRepository;
        this.userContextRepository = userContextRepository;
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
        if (ritualPackRepository.count() == 0) {
            initializeRitualPacks();
        }
        if (userContextRepository.count() == 0) {
            initializeUserContexts();
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

                // Deserialize JSON to List<Ritual>
                List<Ritual> rituals = objectMapper.readValue(json, new TypeReference<List<Ritual>>() {});

                // Save all rituals
                ritualRepository.saveAll(rituals);
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize rituals from JSON file", e);
            }
        }
    }

    private void initializeRitualPacks() {
        try {
            // Read JSON file from resources
            ClassPathResource resource = new ClassPathResource("data/ritualPacks.json");
            String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            // Seed model structure: we reference rituals by title in JSON for convenience
            List<RitualPackSeed> seeds = objectMapper.readValue(json, new TypeReference<List<RitualPackSeed>>() {});

            for (RitualPackSeed seed : seeds) {
                RitualPack pack = new RitualPack();
                pack.setTitle(seed.title);
                pack.setShortDescription(seed.shortDescription);
                pack.setFullDescription(seed.fullDescription);
                pack.setSemanticSummary(seed.semanticSummary);
                pack.setStatus(seed.status);
                pack.setCreatedBy(seed.createdBy);

                // Resolve rituals by title
                List<Ritual> rituals = ritualRepository.findAllByTitleIn(seed.ritualTitles != null ? seed.ritualTitles : Collections.emptyList());
                pack.setRituals(rituals);

                // Aggregate tags from child rituals
                pack.setRitualTypes(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getRitualTypes()).orElse(Collections.emptyList()).stream())
                        .distinct().collect(Collectors.toList()));
                pack.setRitualTones(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getRitualTones()).orElse(Collections.emptyList()).stream())
                        .distinct().collect(Collectors.toList()));
                pack.setLoveTypesSupported(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getLoveTypesSupported()).orElse(Collections.emptyList()).stream())
                        .distinct().collect(Collectors.toList()));
                pack.setEmotionalStatesSupported(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getEmotionalStatesSupported()).orElse(Collections.emptyList()).stream())
                        .distinct().collect(Collectors.toList()));
                pack.setRelationalNeedsServed(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getRelationalNeedsServed()).orElse(Collections.emptyList()).stream())
                        .distinct().collect(Collectors.toList()));
                pack.setLifeContextsRelevant(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getLifeContextsRelevant()).orElse(Collections.emptyList()).stream())
                        .distinct().collect(Collectors.toList()));

                ritualPackRepository.save(pack);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize ritual packs from JSON file", e);
        }
    }

    private void initializeUserContexts() {
        try {
            // Read JSON file from resources
            ClassPathResource resource = new ClassPathResource("data/userContexts.json");
            String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            // Deserialize JSON to List<UserContext>
            List<UserContext> userContexts = objectMapper.readValue(
                json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, UserContext.class)
            );

            // Save all user contexts
            userContextRepository.saveAll(userContexts);
            log.info("Sample user contexts initialized successfully ({} records)", userContexts.size());
        } catch (IOException e) {
            log.error("Failed to initialize user contexts from JSON file", e);
            throw new RuntimeException("Failed to initialize user contexts from JSON file", e);
        }
    }
    
    // Helper seed-only structure
    private static class RitualPackSeed {
        public String title;
        public String shortDescription;
        public String fullDescription;
        public List<String> ritualTitles;
        public String semanticSummary;
        public String createdBy;
        public com.lovingapp.loving.model.enums.PublicationStatus status;
    }
}
