package com.lovingapp.loving.service.ai;

import org.springframework.stereotype.Service;

import com.lovingapp.loving.model.dto.UserContextDTO;

@Service
public class RitualRecommendationService {

    /**
     * Temporary stub. Returns true when minimal context present.
     * Replace with real ritual recommendation engine call later.
     */
    public boolean triggerRecommendations(UserContextDTO context) {
        if (context == null)
            return false;
        boolean hasEmotions = context.getEmotionalStates() != null && !context.getEmotionalStates().isEmpty();
        boolean hasNeeds = context.getRelationalNeeds() != null && !context.getRelationalNeeds().isEmpty();
        boolean hasLoveLang = context.getPreferredLoveLanguages() != null
                && !context.getPreferredLoveLanguages().isEmpty();
        return hasEmotions || hasNeeds || hasLoveLang;
    }
}
