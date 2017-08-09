package uk.gov.ida.verifyserviceprovider;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import io.dropwizard.Application;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.factories.VerifyServiceProviderFactory;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;
import uk.gov.ida.verifyserviceprovider.resources.GenerateAuthnRequestResource;

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
        IdaSamlBootstrap.bootstrap();
        bootstrap.getObjectMapper().setDateFormat(ISO8601DateFormat.getInstance());
    }

    @Override
    public String getName() {
        return "verify-service-provider";
    }

    @Override
    public void run(VerifyServiceProviderConfiguration configuration, Environment environment) throws Exception {
        AuthnRequestFactory authnRequestFactory = new AuthnRequestFactory(
                configuration.getHubSsoLocation(),
                configuration.getServiceEntityId(),
                configuration.getSamlSigningKey());
        VerifyServiceProviderFactory factory = new VerifyServiceProviderFactory(configuration, environment);

        environment.jersey().register(new GenerateAuthnRequestResource(authnRequestFactory, configuration.getHubSsoLocation()));
        environment.jersey().register(factory.getTranslateSamlResponseResource());


        environment.healthChecks().register("hubMetadata", factory.getHubMetadataHealthCheck());
        environment.healthChecks().register("msaMetadata", factory.getMsaMetadataHealthCheck());
    }

    private ConfigurationSourceProvider getFileConfigurationSourceProvider(Bootstrap<VerifyServiceProviderConfiguration> bootstrap) {
        if (fileSystemConfig) {
            return bootstrap.getConfigurationSourceProvider();
        } else {
            return new ResourceConfigurationSourceProvider();
        }
    }
}
