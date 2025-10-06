package com.lovingapp.loving.controller;

import com.lovingapp.loving.model.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/tags")
public class RitualTagsController {

    @GetMapping
    public Map<String, List<String>> allTags() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("ritualTypes", enumNames(RitualType.values()));
        map.put("ritualModes", enumNames(RitualMode.values()));
        map.put("ritualTones", enumNames(RitualTone.values()));
        map.put("sensitivityLevels", enumNames(SensitivityLevel.values()));
        map.put("effortLevels", enumNames(EffortLevel.values()));
        map.put("loveTypes", enumNames(LoveType.values()));
        map.put("emotionalStates", enumNames(EmotionalState.values()));
        map.put("relationalNeeds", enumNames(RelationalNeed.values()));
        map.put("lifeContexts", enumNames(LifeContext.values()));
        map.put("rhythms", enumNames(Rhythm.values()));
        map.put("publicationStatuses", enumNames(PublicationStatus.values()));
        return map;
    }

    @GetMapping("/ritual-types")
    public List<String> ritualTypes() { return enumNames(RitualType.values()); }

    @GetMapping("/ritual-modes")
    public List<String> ritualModes() { return enumNames(RitualMode.values()); }

    @GetMapping("/ritual-tones")
    public List<String> ritualTones() { return enumNames(RitualTone.values()); }

    @GetMapping("/sensitivity-levels")
    public List<String> sensitivityLevels() { return enumNames(SensitivityLevel.values()); }

    @GetMapping("/effort-levels")
    public List<String> effortLevels() { return enumNames(EffortLevel.values()); }

    @GetMapping("/love-types")
    public List<String> loveTypes() { return enumNames(LoveType.values()); }

    @GetMapping("/emotional-states")
    public List<String> emotionalStates() { return enumNames(EmotionalState.values()); }

    @GetMapping("/relational-needs")
    public List<String> relationalNeeds() { return enumNames(RelationalNeed.values()); }

    @GetMapping("/life-contexts")
    public List<String> lifeContexts() { return enumNames(LifeContext.values()); }

    @GetMapping("/rhythms")
    public List<String> rhythms() { return enumNames(Rhythm.values()); }

    @GetMapping("/publication-statuses")
    public List<String> publicationStatuses() { return enumNames(PublicationStatus.values()); }

    private static <E extends Enum<E>> List<String> enumNames(E[] values) {
        return Arrays.stream(values).map(Enum::name).collect(Collectors.toList());
    }
}
