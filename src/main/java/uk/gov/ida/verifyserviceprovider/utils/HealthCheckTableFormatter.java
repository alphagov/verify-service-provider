package uk.gov.ida.verifyserviceprovider.utils;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;

import java.util.List;
import java.util.Optional;

import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.toList;

public class HealthCheckTableFormatter {

    public static String format(HealthCheckRegistry healthCheckRegistry) {
        return StringTableFormatter.format(
            100,
            "Healthcheck status:",
            extractHealthCheckData(healthCheckRegistry)
        );
    }

    private static List<String> extractHealthCheckData(HealthCheckRegistry healthCheckRegistry) {
        return healthCheckRegistry.runHealthChecks().entrySet().stream()
            .map(healthCheckItem -> extractHealthCheckItemRows(healthCheckItem.getKey(), healthCheckItem.getValue()))
            .collect(toList());
    }

    private static String extractHealthCheckItemRows(String healthCheckName, HealthCheck.Result healthCheckResult) {
        String healthCheckNameLine = healthCheckName + ":" + lineSeparator();
        String healthyStatusLine = "|     healthy: " + healthCheckResult.isHealthy();
        String messageLine = Optional.ofNullable(healthCheckResult.getMessage())
            .map(message -> lineSeparator() + "|     message: " + message)
            .orElse("");

        return healthCheckNameLine +
            healthyStatusLine +
            messageLine;
    }
}
