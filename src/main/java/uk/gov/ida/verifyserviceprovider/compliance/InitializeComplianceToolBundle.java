package uk.gov.ida.verifyserviceprovider.compliance;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import uk.gov.ida.verifyserviceprovider.compliance.domain.MatchingDataset;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

public class InitializeComplianceToolBundle implements ConfiguredBundle<VerifyServiceProviderConfiguration> {
    private String url;
    private Integer timeout;
    private MatchingDataset matchingDataset;

    public InitializeComplianceToolBundle(String url, Integer timeout, MatchingDataset matchingDataset) {
        this.url = url;
        this.timeout = timeout;
        this.matchingDataset = matchingDataset;
    }

    @Override
    public void run(VerifyServiceProviderConfiguration configuration, Environment environment) throws Exception {
        ComplianceToolModeConfiguration complianceToolModeConfiguration = (ComplianceToolModeConfiguration) configuration;

        ComplianceToolService complianceToolService = complianceToolModeConfiguration.createComplianceToolService(environment, url, timeout);
        complianceToolService.initializeComplianceTool(matchingDataset);

        environment.jersey().register(new RefreshDatasetResource(complianceToolService));
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }
}
