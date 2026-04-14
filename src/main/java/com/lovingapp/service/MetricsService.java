package com.lovingapp.service;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public <T> T recordTime(String metricName, Callable<T> operation, String... tags) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            T result = operation.call();

            sample.stop(
                    Timer.builder(metricName)
                            .tags(toTags(tags))
                            .tag("status", "success")
                            .register(meterRegistry));

            return result;

        } catch (Exception e) {
            sample.stop(
                    Timer.builder(metricName)
                            .tags(toTags(tags))
                            .tag("status", "error")
                            .register(meterRegistry));

            log.error("Error executing operation for metric: {}", metricName, e);

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    public void recordTime(String metricName, Runnable operation, String... tags) {
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            operation.run();

            sample.stop(
                    Timer.builder(metricName)
                            .tags(toTags(tags))
                            .tag("status", "success")
                            .register(meterRegistry));

        } catch (Exception e) {
            sample.stop(
                    Timer.builder(metricName)
                            .tags(toTags(tags))
                            .tag("status", "error")
                            .register(meterRegistry));

            log.error("Error executing operation for metric: {}", metricName, e);

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    private Iterable<Tag> toTags(String... tags) {
        if (tags == null || tags.length == 0) {
            return Tags.empty();
        }

        if (tags.length % 2 != 0) {
            throw new IllegalArgumentException("Tags must be provided in key-value pairs");
        }

        Tags result = Tags.empty();
        for (int i = 0; i < tags.length; i += 2) {
            result = result.and(tags[i], tags[i + 1]);
        }

        return result;
    }
}
