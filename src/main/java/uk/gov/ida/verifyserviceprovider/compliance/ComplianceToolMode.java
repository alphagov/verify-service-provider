package uk.gov.ida.verifyserviceprovider.compliance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import uk.gov.ida.verifyserviceprovider.compliance.domain.MatchingDataset;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.validation.Validator;

public class ComplianceToolMode extends ServerCommand<VerifyServiceProviderConfiguration> {

    public static final String MATCHING_DATASET = "matchingDataset";
    public static final String ASSERTION_CONSUMER_URL = "assertionConsumerUrl";
    public static final String TIMEOUT = "timeout";
    private static final String PORT = "port";
    private static final String BIND_HOST = "bindHost";
    private MatchingDatasetArgumentResolver matchingDatasetArgumentResolver;

    public ComplianceToolMode(ObjectMapper objectMapper, Validator validator, Application<VerifyServiceProviderConfiguration> application) {
        super(application, "development", "Run the VSP in development mode");
        this.matchingDatasetArgumentResolver = new MatchingDatasetArgumentResolver(objectMapper, validator);
    }


    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-d", "--matchingDataset")
                .dest(MATCHING_DATASET)
                .type(matchingDatasetArgumentResolver)
                .required(true)
                .help("The Matching Dataset that the Compliance Tool will be initialized with");

        subparser.addArgument("-u", "--url")
                .dest(ASSERTION_CONSUMER_URL)
                .type(String.class)
                .required(false)
                .setDefault("http://localhost:8080/SAML2/Response")
                .help("The URL where the Compliance Tool will send responses");

        subparser.addArgument("-t", "--timeout")
                .dest(TIMEOUT)
                .type(Integer.class)
                .required(false)
                .setDefault(5)
                .help("The timeout in seconds when communicating with the Compliance Tool");

        subparser.addArgument("-p", "--port")
                .dest(PORT)
                .type(Integer.class)
                .required(false)
                .setDefault(50300)
                .help("The port that this service will use");

        subparser.addArgument("--host")
                .dest(BIND_HOST)
                .type(String.class)
                .required(false)
                .setDefault("0.0.0.0")
                .help("The host that this service will bind to");

    }

    @Override
    public void run(Bootstrap<?> wildcardBootstrap, Namespace namespace) throws Exception {
        Bootstrap<VerifyServiceProviderConfiguration> bootstrap = (Bootstrap<VerifyServiceProviderConfiguration>) wildcardBootstrap;
        run(namespace, bootstrap);
        super.run(bootstrap, namespace);
    }

    private void run(Namespace namespace, Bootstrap<VerifyServiceProviderConfiguration> bootstrap) {
        String url = namespace.get(ASSERTION_CONSUMER_URL);
        Integer timeout = namespace.get(TIMEOUT);
        MatchingDataset matchingDataset = namespace.get(MATCHING_DATASET);
        Integer port = namespace.get(PORT);
        String bindHost = namespace.get(BIND_HOST);

        bootstrap.addBundle(new InitializeComplianceToolBundle(url, timeout, matchingDataset));
        bootstrap.setConfigurationFactoryFactory(complianceToolModeConfigurationFactory(port, bindHost));
    }

    private ConfigurationFactoryFactory<VerifyServiceProviderConfiguration> complianceToolModeConfigurationFactory(int port, String bindHost) {
        return (klass, validator, objectMapper, propertyPrefix) ->
                new ComplianceToolModeConfigurationFactory(port, bindHost);
    }

}
