package com.lovingapp.loving.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lovingapp.loving.model.entity.LoveTypeInfo;
import com.lovingapp.loving.model.entity.Ritual;
import com.lovingapp.loving.model.entity.RitualHistory;
import com.lovingapp.loving.model.entity.RitualPack;
import com.lovingapp.loving.model.entity.User;
import com.lovingapp.loving.model.entity.UserContext;
import com.lovingapp.loving.model.enums.EmojiFeedback;
import com.lovingapp.loving.model.enums.RitualHistoryStatus;
import com.lovingapp.loving.repository.LoveTypeRepository;
import com.lovingapp.loving.repository.RitualHistoryRepository;
import com.lovingapp.loving.repository.RitualPackRepository;
import com.lovingapp.loving.repository.RitualRepository;
import com.lovingapp.loving.repository.UserContextRepository;
import com.lovingapp.loving.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!test") // Don't run in tests
public class DataInitializer {

    private final LoveTypeRepository loveTypeRepository;
    private final RitualRepository ritualRepository;
    private final RitualPackRepository ritualPackRepository;
    private final UserContextRepository userContextRepository;
    private final RitualHistoryRepository ritualHistoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public DataInitializer(LoveTypeRepository loveTypeRepository,
            RitualRepository ritualRepository,
            RitualPackRepository ritualPackRepository,
            UserContextRepository userContextRepository,
            UserRepository userRepository,
            RitualHistoryRepository ritualHistoryRepository,
            ObjectMapper objectMapper) {
        this.loveTypeRepository = loveTypeRepository;
        this.ritualRepository = ritualRepository;
        this.ritualPackRepository = ritualPackRepository;
        this.userContextRepository = userContextRepository;
        this.userRepository = userRepository;
        this.ritualHistoryRepository = ritualHistoryRepository;
        this.objectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule());
    }

    private String loadJsonFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    @PostConstruct
    @Transactional
    public void init() throws IOException {
        objectMapper.registerModule(new JavaTimeModule());

        // Load users
        if (userRepository.count() == 0) {
            log.info("Loading users...");
            String usersJson = loadJsonFile("data/users.json");
            List<User> users = objectMapper.readValue(usersJson, new TypeReference<>() {
            });
            userRepository.saveAll(users);
            log.info("Loaded {} users", users.size());
        }

        // Load love types
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
        if (ritualHistoryRepository.count() == 0) {
            initializeRitualHistories();
        }
    }

    private void initializeRitualHistories() {
        try {
            // Read JSON file from resources
            ClassPathResource resource = new ClassPathResource("data/ritualHistories.json");
            String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

            // Deserialize JSON to List<RitualHistorySeed>
            List<RitualHistorySeed> seeds = objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, RitualHistorySeed.class));

            // Resolve ritual titles to IDs in a single query
            List<String> ritualTitles = seeds.stream()
                    .map(s -> s.ritualTitle)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            Map<String, UUID> ritualIdByTitle = ritualRepository.findAllByTitleIn(ritualTitles).stream()
                    .collect(Collectors.toMap(Ritual::getTitle, Ritual::getId));

            // Resolve ritual pack titles to IDs in a single query
            List<String> packTitles = seeds.stream()
                    .map(s -> s.ritualPackTitle)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            Map<String, UUID> packIdByTitle = ritualPackRepository.findAllByTitleIn(packTitles).stream()
                    .collect(Collectors.toMap(RitualPack::getTitle, RitualPack::getId));

            List<RitualHistory> histories = new ArrayList<>();
            for (RitualHistorySeed seed : seeds) {
                UUID ritualId = ritualIdByTitle.get(seed.ritualTitle);
                if (ritualId == null) {
                    // Skip if ritual title cannot be resolved
                    continue;
                }

                // Resolve ritualPackId using seed title or fallback mapping
                UUID ritualPackId = null;
                if (seed.ritualPackTitle != null) {
                    ritualPackId = packIdByTitle.get(seed.ritualPackTitle);
                }

                RitualHistory rh = RitualHistory.builder()
                        .userId(seed.userId)
                        .ritualId(ritualId)
                        .ritualPackId(ritualPackId)
                        .status(seed.status != null ? RitualHistoryStatus.valueOf(seed.status)
                                : RitualHistoryStatus.SUGGESTED)
                        .feedback(seed.feedback != null ? EmojiFeedback.valueOf(seed.feedback) : null)
                        .build();
                histories.add(rh);
            }

            if (!histories.isEmpty()) {
                ritualHistoryRepository.saveAll(histories);
                log.info("Sample ritual histories initialized successfully ({} records)", histories.size());
            }
        } catch (IOException e) {
            log.error("Failed to initialize ritual histories from JSON file", e);
            throw new RuntimeException("Failed to initialize ritual histories from JSON file", e);
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
                        objectMapper.getTypeFactory().constructCollectionType(List.class, LoveTypeInfo.class));

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
                List<Ritual> rituals = objectMapper.readValue(json, new TypeReference<List<Ritual>>() {
                });

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
            List<RitualPackSeed> seeds = objectMapper.readValue(json, new TypeReference<List<RitualPackSeed>>() {
            });

            for (RitualPackSeed seed : seeds) {
                RitualPack pack = new RitualPack();
                pack.setTitle(seed.title);
                pack.setShortDescription(seed.shortDescription);
                pack.setFullDescription(seed.fullDescription);
                pack.setSemanticSummary(seed.semanticSummary);
                pack.setStatus(seed.status);
                pack.setCreatedBy(seed.createdBy);

                // Resolve rituals by title
                List<Ritual> rituals = ritualRepository
                        .findAllByTitleIn(seed.ritualTitles != null ? seed.ritualTitles : Collections.emptyList());
                pack.setRituals(rituals);

                // Aggregate tags from child rituals
                pack.setRitualTypes(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getRitualTypes()).orElse(Collections.emptyList()).stream())
                        .distinct().collect(Collectors.toList()));
                pack.setRitualTones(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getRitualTones()).orElse(Collections.emptyList()).stream())
                        .distinct().collect(Collectors.toList()));
                pack.setLoveTypesSupported(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getLoveTypesSupported()).orElse(Collections.emptyList())
                                .stream())
                        .distinct().collect(Collectors.toList()));
                pack.setEmotionalStatesSupported(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getEmotionalStatesSupported())
                                .orElse(Collections.emptyList()).stream())
                        .distinct().collect(Collectors.toList()));
                pack.setRelationalNeedsServed(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getRelationalNeedsServed()).orElse(Collections.emptyList())
                                .stream())
                        .distinct().collect(Collectors.toList()));
                pack.setLifeContextsRelevant(rituals.stream()
                        .flatMap(r -> Optional.ofNullable(r.getLifeContextsRelevant()).orElse(Collections.emptyList())
                                .stream())
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
                    objectMapper.getTypeFactory().constructCollectionType(List.class, UserContext.class));

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

    // Helper seed-only structure for ritual histories
    private static class RitualHistorySeed {
        public java.util.UUID userId;
        public String ritualTitle;
        public String ritualPackTitle;
        public String status;
        public String feedback;
    }
}
