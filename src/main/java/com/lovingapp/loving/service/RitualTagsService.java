package com.lovingapp.loving.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.lovingapp.loving.model.enums.EffortLevel;
import com.lovingapp.loving.model.enums.EmotionalState;
import com.lovingapp.loving.model.enums.LifeContext;
import com.lovingapp.loving.model.enums.LoveType;
import com.lovingapp.loving.model.enums.PublicationStatus;
import com.lovingapp.loving.model.enums.RelationalNeed;
import com.lovingapp.loving.model.enums.Rhythm;
import com.lovingapp.loving.model.enums.RitualMode;
import com.lovingapp.loving.model.enums.RitualTone;
import com.lovingapp.loving.model.enums.RitualType;
import com.lovingapp.loving.model.enums.SensitivityLevel;

@Service
public class RitualTagsService {

    public Map<String, List<String>> getAllTags() {
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

    private static <E extends Enum<E>> List<String> enumNames(E[] values) {
        return Arrays.stream(values).map(Enum::name).collect(Collectors.toList());
    }
}
