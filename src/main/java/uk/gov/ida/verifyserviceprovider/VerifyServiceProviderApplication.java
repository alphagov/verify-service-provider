package uk.gov.ida.verifyserviceprovider;

import io.dropwizard.Application;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.resources.GenerateAuthnRequestResource;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseResource;

import java.util.Arrays;

public class VerifyServiceProviderApplication extends Application<VerifyServiceProviderConfiguration> {
    private boolean fileSystemConfig;

    private VerifyServiceProviderApplication(boolean fileSystemConfig) {
        this.fileSystemConfig = fileSystemConfig;
    }

    public VerifyServiceProviderApplication() {
        this(true);
    }

    public static void main(String[] args) throws Exception {
        if (Arrays.asList(args).isEmpty()) {
            new VerifyServiceProviderApplication(false).run("server", "verify-service-provider-env.yml");
        } else {
            new VerifyServiceProviderApplication().run(args);
        }
    }

    @Override
    public void initialize(Bootstrap<VerifyServiceProviderConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(getFileConfigurationSourceProvider(bootstrap),
                new EnvironmentVariableSubstitutor(false)
            )
        );
    }

    @Override
    public String getName() {
        return "verify-service-provider";
    }

    @Override
    public void run(VerifyServiceProviderConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new GenerateAuthnRequestResource(configuration));
        environment.jersey().register(new TranslateSamlResponseResource());
    }

    public ConfigurationSourceProvider getFileConfigurationSourceProvider(Bootstrap<VerifyServiceProviderConfiguration> bootstrap) {
        if (fileSystemConfig) {
            return bootstrap.getConfigurationSourceProvider();
        } else {
            return new ResourceConfigurationSourceProvider();
        }
    }
}
