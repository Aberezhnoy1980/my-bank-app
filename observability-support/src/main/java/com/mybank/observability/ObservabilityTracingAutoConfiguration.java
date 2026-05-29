package com.mybank.observability;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@ConditionalOnClass(name = "io.micrometer.tracing.Tracer")
@PropertySource("classpath:application-observability.yml")
public class ObservabilityTracingAutoConfiguration {
}
