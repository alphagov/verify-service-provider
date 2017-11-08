package uk.gov.ida.verifyserviceprovider.utils;

import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;

import java.io.File;

public class ConfigurationFileFinder {
    public static String getConfigurationFilePath() {
        String applicationClassPath =
            VerifyServiceProviderApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String configFilePath = applicationClassPath
            .replaceFirst("lib/verify-service-provider-.*\\.jar$", "verify-service-provider.yml")
            .replaceFirst("build/classes/main/$", "verify-service-provider.yml");

        if (new File(configFilePath).exists()) {
            return configFilePath;
        }

        throw new RuntimeException(
            String.format(
                "Unable to locate configuration file at %s. " +
                "Please ensure a config file is present here, or provide location as a command line argument",
                configFilePath)
        );
    }
}
