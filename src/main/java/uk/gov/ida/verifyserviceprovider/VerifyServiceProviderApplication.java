package uk.gov.ida.verifyserviceprovider;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.verifyserviceprovider.resources.HelloWorldResource;

public class VerifyServiceProviderApplication extends Application<VerifyServiceProviderConfiguration> {
    public static void main(String[] args) throws Exception {
        new VerifyServiceProviderApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<VerifyServiceProviderConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
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
        environment.jersey().register(new HelloWorldResource(configuration));
    }
}
