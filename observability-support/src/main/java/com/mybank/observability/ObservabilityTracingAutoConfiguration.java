package com.mybank.observability;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * Activates when Micrometer Tracing is on the classpath.
 * Shared defaults are imported via {@code spring.config.import} in {@code application.properties}.
 */
@AutoConfiguration
@ConditionalOnClass(name = "io.micrometer.tracing.Tracer")
public class ObservabilityTracingAutoConfiguration {
}
