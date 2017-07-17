package uk.gov.ida.verifyserviceprovider.utils;

import com.codahale.metrics.health.HealthCheck;

public class ApplicationHealthCheck extends HealthCheck {
    public String getName() {
        return "applicationHealthCheck";
    }

    @Override
    protected Result check() {
        return Result.healthy();
    }
}
