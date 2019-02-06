package uk.gov.ida.verifyserviceprovider.compliance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.Application;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDataset;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.validation.Validator;
import java.io.IOException;
import java.net.URL;

public class ComplianceToolMode extends ServerCommand<VerifyServiceProviderConfiguration> {

    static final String IDENTITY_DATASET = "identityDataset";
    static final String ASSERTION_CONSUMER_URL = "assertionConsumerUrl";
    static final String TIMEOUT = "timeout";
    static final String PORT = "port";
    static final String BIND_HOST = "bindHost";
    static final int DEFAULT_TIMEOUT = 5;
    static final int DEFAULT_PORT = 50300;
    static final String DEFAULT_CONSUMER_URL = "http://localhost:8080/SAML2/Response";
    static final String DEFAULT_HOST = "0.0.0.0";
    private final MatchingDataset defaultIdentityDataset;

    private IdentityDatasetArgumentResolver identityDatasetArgumentResolver;

    public ComplianceToolMode(ObjectMapper objectMapper, Validator validator, Application<VerifyServiceProviderConfiguration> application) {
        super(application, "development", "Run the VSP in development mode");
        this.identityDatasetArgumentResolver = new IdentityDatasetArgumentResolver(objectMapper, validator);
        this.defaultIdentityDataset = createDefaultIdentityDataset(objectMapper);
    }


    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-d", "--identityDataset")
                .dest(IDENTITY_DATASET)
                .type(identityDatasetArgumentResolver)
                .setDefault(defaultIdentityDataset)
                .help("The identity dataset that the Compliance Tool will be initialized with");

        subparser.addArgument("-u", "--url")
                .dest(ASSERTION_CONSUMER_URL)
                .type(String.class)
                .required(false)
                .setDefault(DEFAULT_CONSUMER_URL)
                .help("The URL where the Compliance Tool will send responses");

        subparser.addArgument("-t", "--timeout")
                .dest(TIMEOUT)
                .type(Integer.class)
                .required(false)
                .setDefault(DEFAULT_TIMEOUT)
                .help("The timeout in seconds when communicating with the Compliance Tool");

        subparser.addArgument("-p", "--port")
                .dest(PORT)
                .type(Integer.class)
                .required(false)
                .setDefault(DEFAULT_PORT)
                .help("The port that this service will use");

        subparser.addArgument("--host")
                .dest(BIND_HOST)
                .type(String.class)
                .required(false)
                .setDefault(DEFAULT_HOST)
                .help("The host that this service will bind to");

    }

    @Override
    public void run(Bootstrap<?> wildcardBootstrap, Namespace namespace) throws Exception {
        Integer port = namespace.get(PORT);
        String bindHost = namespace.get(BIND_HOST);

        Bootstrap<VerifyServiceProviderConfiguration> bootstrap = (Bootstrap<VerifyServiceProviderConfiguration>) wildcardBootstrap;
        bootstrap.setConfigurationFactoryFactory(complianceToolModeConfigurationFactory(port, bindHost));
        super.run(bootstrap, namespace);
    }

    @Override
    protected void run(Environment environment, Namespace namespace, VerifyServiceProviderConfiguration configuration) throws Exception {
        String url = namespace.get(ASSERTION_CONSUMER_URL);
        Integer timeout = namespace.get(TIMEOUT);
        MatchingDataset matchingDataset = namespace.get(IDENTITY_DATASET);

        ComplianceToolModeConfiguration complianceToolModeConfiguration = (ComplianceToolModeConfiguration) configuration;

        ComplianceToolClient complianceToolClient = complianceToolModeConfiguration.createComplianceToolService(environment, url, timeout);
        complianceToolClient.initializeComplianceTool(matchingDataset);

        environment.jersey().register(new RefreshDatasetResource(complianceToolClient));
        super.run(environment, namespace, configuration);
    }

    private ConfigurationFactoryFactory<VerifyServiceProviderConfiguration> complianceToolModeConfigurationFactory(int port, String bindHost) {
        return (klass, validator, objectMapper, propertyPrefix) ->
                new ComplianceToolModeConfigurationFactory(port, bindHost);
    }

    private static MatchingDataset createDefaultIdentityDataset(ObjectMapper objectMapper) {
        URL resource = Resources.getResource("default-test-identity-dataset.json");
        try {
            return objectMapper.readValue(resource, DefaultIdentityDataset.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class DefaultIdentityDataset extends MatchingDataset {
        public DefaultIdentityDataset() {
        }

        @Override
        public String toString() {
            return "See the README for a description of this field";
        }
    }
}
