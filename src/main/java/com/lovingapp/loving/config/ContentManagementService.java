package com.lovingapp.loving.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovingapp.loving.model.dto.RitualDTO;
import com.lovingapp.loving.model.dto.RitualPackDTO;
import com.lovingapp.loving.model.entity.LoveTypeInfo;
import com.lovingapp.loving.model.enums.PublicationStatus;
import com.lovingapp.loving.service.LoveTypeService;
import com.lovingapp.loving.service.RitualPackService;
import com.lovingapp.loving.service.RitualService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ContentManagementService implements CommandLineRunner {

    @Autowired
    private RitualService ritualService;

    @Autowired
    private RitualPackService ritualPackService;

    @Autowired
    private LoveTypeService loveTypeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        syncRituals();
        syncRitualPacks();
        syncLoveTypes();
    }

    private void syncRituals() {
        try {
            log.info("Starting ritual synchronization...");
            String json = loadJsonFile("data/rituals.json");
            List<RitualDTO> rituals = objectMapper.readValue(json, new TypeReference<List<RitualDTO>>() {
            });

            // Get existing rituals and map by ID
            Map<UUID, RitualDTO> dbById = ritualService.findAll().stream()
                    .collect(Collectors.toMap(RitualDTO::getId, r -> r));

            List<RitualDTO> toCreate = new ArrayList<>();
            List<RitualDTO> toUpdate = new ArrayList<>();
            Set<UUID> activeRitualIds = new HashSet<>();

            // Process each ritual from JSON
            for (RitualDTO dto : rituals) {
                if (dto.getId() == null) {
                    continue;
                }

                activeRitualIds.add(dto.getId());
                String newHash = computeRitualHash(dto);
                RitualDTO existing = dbById.get(dto.getId());

                if (existing == null) {
                    dto.setContentHash(newHash);
                    toCreate.add(dto);
                } else if (isDifferent(existing.getContentHash(), newHash)) {
                    dto.setContentHash(newHash);
                    toUpdate.add(dto);
                }
            }

            // Determine deletions (mark as ARCHIVED)
            List<RitualDTO> toDelete = dbById.values().stream()
                    .filter(existing -> !activeRitualIds.contains(existing.getId())
                            && existing.getStatus() != PublicationStatus.ARCHIVED)
                    .peek(existing -> {
                        existing.setStatus(PublicationStatus.ARCHIVED);
                        existing.setContentHash(computeRitualHash(existing));
                    })
                    .collect(Collectors.toList());

            // merge updates and deletions into a single bulk update
            List<RitualDTO> toUpdateAndDelete = new ArrayList<>();
            toUpdateAndDelete.addAll(toUpdate);
            toUpdateAndDelete.addAll(toDelete);

            // Bulk operations
            ritualService.bulkCreate(toCreate);
            ritualService.bulkUpdate(toUpdateAndDelete);

            log.info("Ritual synchronization completed - Created: {}, Updated: {}, Archived: {}",
                    toCreate.size(), toUpdate.size(), toDelete.size());

        } catch (Exception e) {
            log.error("Failed to synchronize rituals", e);
            throw new RuntimeException("Failed to initialize rituals from JSON file", e);
        }
    }

    private void syncLoveTypes() {
        try {
            log.info("Starting love type synchronization...");
            String json = loadJsonFile("data/loveTypes.json");
            List<LoveTypeInfo> loveTypes = objectMapper.readValue(json, new TypeReference<List<LoveTypeInfo>>() {
            });

            Map<Integer, LoveTypeInfo> dbById = loveTypeService.findAll().stream()
                    .collect(Collectors.toMap(LoveTypeInfo::getId, lt -> lt));

            int created = 0;
            int updated = 0;

            for (LoveTypeInfo dto : loveTypes) {
                if (dto.getId() == null) {
                    continue;
                }

                String newHash = computeLoveTypeHash(dto);
                LoveTypeInfo existing = dbById.get(dto.getId());

                if (existing == null) {
                    dto.setContentHash(newHash);
                    loveTypeService.save(dto);
                    created++;
                } else if (isDifferent(existing.getContentHash(), newHash)) {
                    dto.setContentHash(newHash);
                    loveTypeService.update(dto.getId(), dto);
                    updated++;
                }
            }

            log.info("Love type synchronization completed - Created: {}, Updated: {}", created, updated);

        } catch (Exception e) {
            log.error("Failed to synchronize love types", e);
            throw new RuntimeException("Failed to initialize love types from JSON file", e);
        }
    }

    private void syncRitualPacks() {
        try {
            log.info("Starting ritual pack synchronization...");
            String json = loadJsonFile("data/ritualPacks.json");
            List<RitualPackDTO> packs = objectMapper.readValue(json, new TypeReference<List<RitualPackDTO>>() {
            });

            // Get existing packs and map by ID
            Map<UUID, RitualPackDTO> dbById = ritualPackService.findAll().stream()
                    .collect(Collectors.toMap(RitualPackDTO::getId, p -> p));

            List<RitualPackDTO> toCreate = new ArrayList<>();
            List<RitualPackDTO> toUpdate = new ArrayList<>();
            Set<UUID> activePackIds = new HashSet<>();

            // Process each pack from JSON
            for (RitualPackDTO dto : packs) {
                if (dto.getId() == null) {
                    continue;
                }

                activePackIds.add(dto.getId());
                String newHash = computeRitualPackHash(dto);
                RitualPackDTO existing = dbById.get(dto.getId());

                if (existing == null) {
                    dto.setContentHash(newHash);
                    toCreate.add(dto);
                } else if (isDifferent(existing.getContentHash(), newHash)) {
                    dto.setContentHash(newHash);
                    toUpdate.add(dto);
                }
            }

            // Determine deletions (mark as ARCHIVED)
            List<RitualPackDTO> toDelete = dbById.values().stream()
                    .filter(existing -> !activePackIds.contains(existing.getId())
                            && existing.getStatus() != PublicationStatus.ARCHIVED)
                    .peek(existing -> {
                        existing.setStatus(PublicationStatus.ARCHIVED);
                        existing.setContentHash(computeRitualPackHash(existing));
                    })
                    .collect(Collectors.toList());

            // Bulk operations
            int created = ritualPackService.bulkCreate(toCreate).size();
            // merge updates and deletions into a single bulk update
            toUpdate.addAll(toDelete);
            int updated = ritualPackService.bulkUpdate(toUpdate).size();
            int archived = toDelete.size();

            log.info("Ritual pack synchronization completed - Created: {}, Updated: {}, Archived: {}",
                    created, updated, archived);

        } catch (Exception e) {
            log.error("Failed to synchronize ritual packs", e);
            throw new RuntimeException("Failed to initialize ritual packs from JSON file", e);
        }
    }

    private String loadJsonFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private boolean isDifferent(String oldHash, String newHash) {
        return oldHash == null || !oldHash.equals(newHash);
    }

    private String computeRitualHash(RitualDTO dto) {
        try {
            RitualDTO hashDto = RitualDTO.builder()
                    .title(dto.getTitle())
                    .tagLine(dto.getTagLine())
                    .description(dto.getDescription())
                    .howItHelps(dto.getHowItHelps())
                    .steps(dto.getSteps())
                    .loveTypes(dto.getLoveTypes())
                    .relationalNeeds(dto.getRelationalNeeds())
                    .ritualMode(dto.getRitualMode())
                    .ritualTones(dto.getRitualTones())
                    .timeTaken(dto.getTimeTaken())
                    .mediaAssets(dto.getMediaAssets())
                    .semanticSummary(dto.getSemanticSummary())
                    .status(dto.getStatus())
                    .build();

            String json = objectMapper.writeValueAsString(hashDto);
            return DigestUtils.sha256Hex(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute ritual hash", e);
        }
    }

    private String computeRitualPackHash(RitualPackDTO dto) {
        try {
            RitualPackDTO hashDto = RitualPackDTO.builder()
                    .title(dto.getTitle())
                    .tagLine(dto.getTagLine())
                    .description(dto.getDescription())
                    .howItHelps(dto.getHowItHelps())
                    .ritualIds(dto.getRitualIds())
                    .journey(dto.getJourney())
                    .loveTypes(dto.getLoveTypes())
                    .relationalNeeds(dto.getRelationalNeeds())
                    .mediaAssets(dto.getMediaAssets())
                    .semanticSummary(dto.getSemanticSummary())
                    .status(dto.getStatus())
                    .build();

            String json = objectMapper.writeValueAsString(hashDto);
            return DigestUtils.sha256Hex(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute ritual pack hash", e);
        }
    }

    private String computeLoveTypeHash(LoveTypeInfo dto) {
        try {
            LoveTypeInfo hashDto = LoveTypeInfo.builder()
                    .loveType(dto.getLoveType())
                    .title(dto.getTitle())
                    .subtitle(dto.getSubtitle())
                    .description(dto.getDescription())
                    .sections(dto.getSections())
                    .build();

            String json = objectMapper.writeValueAsString(hashDto);
            return DigestUtils.sha256Hex(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute love type hash", e);
        }
    }
}
