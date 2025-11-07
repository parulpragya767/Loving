package com.lovingapp.loving.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovingapp.loving.model.dto.RitualDTO;
import com.lovingapp.loving.repository.RitualPackRepository;
import com.lovingapp.loving.service.RitualService;

@Component
public class RitualSyncService implements CommandLineRunner {

    @Autowired
    private RitualService ritualService;

    @Autowired
    private RitualPackRepository ritualPackRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        syncRituals();
        syncRitualPacks();
    }

    private void syncRituals() {
        try {
            String json = loadJsonFile("data/rituals.json");
            List<RitualDTO> rituals = objectMapper.readValue(json, new TypeReference<List<RitualDTO>>() {
            });

            Map<UUID, RitualDTO> dbById = ritualService.getAllRituals().stream()
                    .collect(Collectors.toMap(RitualDTO::getId, r -> r));

            for (RitualDTO dto : rituals) {
                if (dto.getId() == null)
                    continue;
                String newHash = computeContentHash(dto);
                RitualDTO existing = dbById.get(dto.getId());
                if (existing == null) {
                    dto.setContentHash(newHash);
                    ritualService.createRitual(dto);
                } else if (isDifferent(existing.getContentHash(), newHash)) {
                    dto.setContentHash(newHash);
                    ritualService.updateRitual(dto.getId(), dto);
                }
            }

            // Optional delete of rituals not present in JSON can go here if needed
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize rituals from JSON file", e);
        }
    }

    private void syncRitualPacks() {
        // similar logic
    }

    private String loadJsonFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }

    private boolean isDifferent(String oldHash, String newHash) {
        return oldHash == null || !oldHash.equals(newHash);
    }

    private String computeContentHash(RitualDTO dto) {
        try {
            Map<String, Object> content = new HashMap<>();
            content.put("title", dto.getTitle());
            content.put("description", dto.getDescription());
            content.put("ritualMode", dto.getRitualMode());
            content.put("ritualTones", dto.getRitualTones());
            content.put("timeTaken", dto.getTimeTaken());
            content.put("steps", dto.getSteps());
            content.put("mediaAssets", dto.getMediaAssets());
            content.put("loveTypes", dto.getLoveTypes());
            content.put("relationalNeeds", dto.getRelationalNeeds());
            content.put("semanticSummary", dto.getSemanticSummary());
            content.put("status", dto.getStatus());

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(content);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute content hash", e);
        }
    }
}
