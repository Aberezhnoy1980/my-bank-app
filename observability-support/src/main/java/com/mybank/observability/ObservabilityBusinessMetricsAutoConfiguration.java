package com.mybank.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
public class ObservabilityBusinessMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    BusinessMetrics businessMetrics(MeterRegistry meterRegistry) {
        return new BusinessMetrics(meterRegistry);
    }
}
