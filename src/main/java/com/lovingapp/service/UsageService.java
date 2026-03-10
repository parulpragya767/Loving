package com.lovingapp.service;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lovingapp.mapper.UserUsageCounterMapper;
import com.lovingapp.model.dto.UserUsageCounterDTOs.UserUsageCounterDTO;
import com.lovingapp.model.entity.UserUsageCounter;
import com.lovingapp.model.enums.UsagePeriodType;
import com.lovingapp.repository.UserUsageCounterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageService {

    private final UserUsageCounterRepository userUsageCounterRepository;

    @Transactional
    public void incrementAiMessageUsage(UUID userId) {
        UserUsageCounter counter = getOrCreateCounterDaily(userId);
        counter.setAiMessagesCount(counter.getAiMessagesCount() + 1);
        userUsageCounterRepository.save(counter);
        log.info("Incremented AI message usage for user: {} to count: {}", userId, counter.getAiMessagesCount());
    }

    @Transactional
    public void incrementRecommendationUsage(UUID userId) {
        UserUsageCounter counter = getOrCreateCounterWeekly(userId);
        counter.setRecommendationsCount(counter.getRecommendationsCount() + 1);
        userUsageCounterRepository.save(counter);
        log.info("Incremented recommendation usage for user: {} to count: {}", userId,
                counter.getRecommendationsCount());
    }

    public UserUsageCounterDTO getDailyUsage(UUID userId) {
        UserUsageCounter counter = getOrCreateCounterDaily(userId);
        return UserUsageCounterMapper.toDto(counter);
    }

    public UserUsageCounterDTO getWeeklyUsage(UUID userId) {
        UserUsageCounter counter = getOrCreateCounterWeekly(userId);
        return UserUsageCounterMapper.toDto(counter);
    }

    @Transactional
    private UserUsageCounter getOrCreateCounterDaily(UUID userId) {
        OffsetDateTime periodStart = getStartOfDay();
        return userUsageCounterRepository
                .findByUserIdAndPeriodTypeAndPeriodStart(userId, UsagePeriodType.DAILY, periodStart)
                .orElseGet(() -> {
                    UserUsageCounter counter = UserUsageCounter.builder()
                            .userId(userId)
                            .periodType(UsagePeriodType.DAILY)
                            .periodStart(periodStart)
                            .aiMessagesCount(0)
                            .recommendationsCount(0)
                            .build();

                    UserUsageCounter saved = userUsageCounterRepository.saveAndFlush(counter);
                    log.info("Created new daily usage counter for period: {}", periodStart);
                    return saved;
                });
    }

    @Transactional
    private UserUsageCounter getOrCreateCounterWeekly(UUID userId) {
        OffsetDateTime periodStart = getStartOfWeek();
        return userUsageCounterRepository
                .findByUserIdAndPeriodTypeAndPeriodStart(userId, UsagePeriodType.WEEKLY, periodStart)
                .orElseGet(() -> {
                    UserUsageCounter counter = UserUsageCounter.builder()
                            .userId(userId)
                            .periodType(UsagePeriodType.WEEKLY)
                            .periodStart(periodStart)
                            .aiMessagesCount(0)
                            .recommendationsCount(0)
                            .build();

                    UserUsageCounter saved = userUsageCounterRepository.saveAndFlush(counter);
                    log.info("Created new weekly usage counter for period: {}", periodStart);
                    return saved;
                });
    }

    private OffsetDateTime getStartOfDay() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return now.toLocalDate()
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);
    }

    private OffsetDateTime getStartOfWeek() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return now.toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);
    }
}
