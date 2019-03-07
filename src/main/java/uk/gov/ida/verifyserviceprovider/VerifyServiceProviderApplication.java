package uk.gov.ida.verifyserviceprovider;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import io.dropwizard.Application;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.dropwizard.logstash.LogstashBundle;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.metadata.bundle.MetadataResolverBundle;
import uk.gov.ida.verifyserviceprovider.compliance.ComplianceToolMode;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JerseyViolationExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JsonProcessingExceptionMapper;
import uk.gov.ida.verifyserviceprovider.factories.VerifyServiceProviderFactory;
import uk.gov.ida.verifyserviceprovider.listeners.VerifyServiceProviderServerListener;
import uk.gov.ida.verifyserviceprovider.utils.ConfigurationFileFinder;

import javax.ws.rs.client.Client;
import java.util.Arrays;
import java.util.Optional;

public class VerifyServiceProviderApplication extends Application<VerifyServiceProviderConfiguration> {

    private MetadataResolverBundle<VerifyServiceProviderConfiguration> hubMetadataBundle;
    private MetadataResolverBundle<VerifyServiceProviderConfiguration> msaMetadataBundle;

    public VerifyServiceProviderApplication() {
        hubMetadataBundle = new MetadataResolverBundle<>(configuration -> Optional.ofNullable(configuration.getVerifyHubMetadata()));
        msaMetadataBundle = new MetadataResolverBundle<>(VerifyServiceProviderConfiguration::getMsaMetadata, false);
    }

    public static void main(String[] args) throws Exception {
        if (Arrays.asList(args).isEmpty()) {
            String configFilePath = ConfigurationFileFinder.getConfigurationFilePath();
            new VerifyServiceProviderApplication().run("server", configFilePath);
        } else {
            new VerifyServiceProviderApplication().run(args);
        }
    }

    @Override
    public void initialize(Bootstrap<VerifyServiceProviderConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)
            )
        );
        IdaSamlBootstrap.bootstrap();
        bootstrap.getObjectMapper().setDateFormat(StdDateFormat.getInstance());
        bootstrap.addBundle(hubMetadataBundle);
        bootstrap.addBundle(msaMetadataBundle);
        bootstrap.addCommand(new ComplianceToolMode(bootstrap.getObjectMapper(), bootstrap.getValidatorFactory().getValidator(), this));
        bootstrap.addBundle(new LogstashBundle());
    }

    @Override
    public String getName() {
        return "verify-service-provider";
    }

    @Override
    public void run(VerifyServiceProviderConfiguration configuration, Environment environment) throws Exception {
        Client client = new JerseyClientBuilder(environment).build(getName());
        VerifyServiceProviderFactory factory = new VerifyServiceProviderFactory(configuration, hubMetadataBundle, msaMetadataBundle, client);

        environment.jersey().register(new JerseyViolationExceptionMapper());
        environment.jersey().register(new JsonProcessingExceptionMapper());
        environment.jersey().register(new InvalidEntityIdExceptionMapper());
        environment.jersey().register(factory.getVersionNumberResource());
        environment.jersey().register(factory.getGenerateAuthnRequestResource());
        environment.jersey().register(factory.getTranslateSamlResponseResource());
        environment.lifecycle().addServerLifecycleListener(new VerifyServiceProviderServerListener(environment));
    }
}
