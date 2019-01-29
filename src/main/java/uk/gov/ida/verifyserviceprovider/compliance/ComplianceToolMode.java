package uk.gov.ida.verifyserviceprovider.compliance;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import uk.gov.ida.verifyserviceprovider.compliance.domain.MatchingAddress;
import uk.gov.ida.verifyserviceprovider.compliance.domain.MatchingAttribute;
import uk.gov.ida.verifyserviceprovider.compliance.domain.MatchingDataset;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.validation.Validator;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class ComplianceToolMode extends ServerCommand<VerifyServiceProviderConfiguration> {

    static final String MATCHING_DATASET = "matchingDataset";
    static final String ASSERTION_CONSUMER_URL = "assertionConsumerUrl";
    static final String TIMEOUT = "timeout";
    static final String PORT = "port";
    static final String BIND_HOST = "bindHost";
    static final int DEFAULT_TIMEOUT = 5;
    static final int DEFAULT_PORT = 50300;
    static final String DEFAULT_CONSUMER_URL = "http://localhost:8080/SAML2/Response";
    static final String DEFAULT_HOST = "0.0.0.0";
    static final MatchingDataset DEFAULT_MATCHING_DATASET = createDefaultMatchingDataset();

    private MatchingDatasetArgumentResolver matchingDatasetArgumentResolver;

    public ComplianceToolMode(ObjectMapper objectMapper, Validator validator, Application<VerifyServiceProviderConfiguration> application) {
        super(application, "development", "Run the VSP in development mode");
        this.matchingDatasetArgumentResolver = new MatchingDatasetArgumentResolver(objectMapper, validator);
    }


    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-d", "--matchingDataset")
                .dest(MATCHING_DATASET)
                .type(matchingDatasetArgumentResolver)
                .setDefault(DEFAULT_MATCHING_DATASET)
                .help("The Matching Dataset that the Compliance Tool will be initialized with");

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
        MatchingDataset matchingDataset = namespace.get(MATCHING_DATASET);

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

    private static MatchingDataset createDefaultMatchingDataset() {
        String standardFromDateString = "2013-02-22T14:32:14.064";
        String standardToDateString = "2015-10-02T09:32:14.967";
        String laterFromDateString = "2015-10-02T09:32:14.967";
        String laterToDateString = "2018-03-03T10:20:50.163";
        LocalDateTime standardFromDate = LocalDateTime.parse(standardFromDateString);
        LocalDateTime standardToDate = LocalDateTime.parse(standardToDateString);
        LocalDateTime laterFromDate = LocalDateTime.parse(laterFromDateString);
        LocalDateTime laterToDate = LocalDateTime.parse(laterToDateString);

        return new MatchingDataset(
                new MatchingAttribute("Default", true, standardFromDate, standardToDate),
                new MatchingAttribute("Person", true, standardFromDate, standardToDate),
                asList(new MatchingAttribute("Smith", true, standardFromDate, standardToDate), new MatchingAttribute("Smythington", true, laterFromDate, laterToDate)),
                new MatchingAttribute("NOT_SPECIFIED", true, standardFromDate, standardToDate),
                new MatchingAttribute("1970-01-01", true, standardFromDate, standardToDate),
                singletonList(new MatchingAddress(true, standardFromDate, standardToDate, "E1 8QS", asList("The White Chapel Building" ,"10 Whitechapel High Street"), null, null)),
                UUID.randomUUID().toString()
        );
    }

}
