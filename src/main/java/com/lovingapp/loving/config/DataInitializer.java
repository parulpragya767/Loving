package com.lovingapp.loving.config;

import com.lovingapp.loving.model.LoveType;
import com.lovingapp.loving.model.Ritual;
import com.lovingapp.loving.repository.LoveTypeRepository;
import com.lovingapp.loving.repository.RitualRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
        // First, delete all existing love types to avoid conflicts
        loveTypeRepository.deleteAll();
        
        // Create and save new love types without predefined IDs
        List<LoveType> loveTypes = Arrays.asList(
            new LoveType("Romantic Love", "Deep emotional connection and passion"),
            new LoveType("Platonic Love", "Non-romantic affection between friends"),
            new LoveType("Familial Love", "Bond between family members"),
            new LoveType("Self-Love", "Appreciation and care for oneself"),
            new LoveType("Companionate Love", "Deep friendship and long-term commitment")
        );
        loveTypeRepository.saveAll(loveTypes);
    }

    private void initializeRituals() {
        // Create some sample rituals with string tags
        Ritual ritual1 = new Ritual();
        ritual1.setTitle("Morning Coffee Together");
        ritual1.setDescription("Start your day with a shared cup of coffee or tea and some quiet time together.");
        ritual1.setTags(Arrays.asList("romantic", "casual", "morning"));

        Ritual ritual2 = new Ritual();
        ritual2.setTitle("Weekly Date Night");
        ritual2.setDescription("Set aside one evening each week for just the two of you to connect.");
        ritual2.setTags(Arrays.asList("romantic", "quality time", "date"));

        Ritual ritual3 = new Ritual();
        ritual3.setTitle("Evening Walk");
        ritual3.setDescription("Take a short walk together after dinner to unwind and talk about your day.");
        ritual3.setTags(Arrays.asList("casual", "relaxing", "exercise"));

        // Save all rituals
        ritualRepository.saveAll(Arrays.asList(ritual1, ritual2, ritual3));
    }
    
}
