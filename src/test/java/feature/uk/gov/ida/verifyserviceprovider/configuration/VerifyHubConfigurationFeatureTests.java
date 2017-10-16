package feature.uk.gov.ida.verifyserviceprovider.configuration;

import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.HubMetadataConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyHubConfiguration;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.PRODUCTION_VERIFY_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.TEST_VERIFY_TRUSTSTORE_NAME;
import static uk.gov.ida.verifyserviceprovider.utils.DefaultObjectMapper.OBJECT_MAPPER;

public class VerifyHubConfigurationFeatureTests {
    @Test
    public void shouldSetExpectedDefaultsForComplianceToolEnvironmentIfNoOverridesGiven() throws Exception {
        String config = "{\"environment\": \"COMPLIANCE_TOOL\"}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);
        HubMetadataConfiguration metadataConfiguration = actualConfiguration.getHubMetadataConfiguration();

        assertThat(actualConfiguration.getHubSsoLocation().toString()).isEqualTo("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/SSO");
        assertThat(metadataConfiguration.getUri().toString()).isEqualTo("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/federation");
        assertThat(metadataConfiguration.getExpectedEntityId()).isEqualTo("https://signin.service.gov.uk");
        assertThat(metadataConfiguration.getTrustStorePassword()).startsWith("bj76");
        assertThat(metadataConfiguration.getTrustStorePath()).endsWith(TEST_VERIFY_TRUSTSTORE_NAME);
        assertThat(metadataConfiguration.getJerseyClientConfiguration()).isNotNull();
        assertThat(metadataConfiguration.getJerseyClientName()).isEqualTo(HUB_JERSEY_CLIENT_NAME);
        assertThat(metadataConfiguration.getMinRefreshDelay()).isEqualTo(60000);
        assertThat(metadataConfiguration.getMaxRefreshDelay()).isEqualTo(600000);
    }

    @Test
    public void shouldSetExpectedDefaultsForIntegrationEnvironmentIfNoOverridesGiven() throws Exception {
        String config = "{\"environment\": \"INTEGRATION\"}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);
        HubMetadataConfiguration metadataConfiguration = actualConfiguration.getHubMetadataConfiguration();

        assertThat(actualConfiguration.getHubSsoLocation().toString()).isEqualTo("https://www.integration.signin.service.gov.uk/SAML2/SSO");
        assertThat(metadataConfiguration.getUri().toString()).isEqualTo("https://www.integration.signin.service.gov.uk/SAML2/metadata/federation");
        assertThat(metadataConfiguration.getExpectedEntityId()).isEqualTo("https://signin.service.gov.uk");
        assertThat(metadataConfiguration.getTrustStorePassword()).startsWith("bj76");
        assertThat(metadataConfiguration.getTrustStorePath()).endsWith(TEST_VERIFY_TRUSTSTORE_NAME);
        assertThat(metadataConfiguration.getJerseyClientConfiguration()).isNotNull();
        assertThat(metadataConfiguration.getJerseyClientName()).isEqualTo(HUB_JERSEY_CLIENT_NAME);
        assertThat(metadataConfiguration.getMinRefreshDelay()).isEqualTo(60000);
        assertThat(metadataConfiguration.getMaxRefreshDelay()).isEqualTo(600000);
    }

    @Test
    public void shouldSetExpectedDefaultsForProductionEnvironmentIfNoOverridesGiven() throws Exception {
        String config = "{\"environment\": \"PRODUCTION\"}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);
        HubMetadataConfiguration metadataConfiguration = actualConfiguration.getHubMetadataConfiguration();

        assertThat(actualConfiguration.getHubSsoLocation().toString()).isEqualTo("https://www.signin.service.gov.uk/SAML2/SSO");
        assertThat(metadataConfiguration.getUri().toString()).isEqualTo("https://www.signin.service.gov.uk/SAML2/metadata/federation");
        assertThat(metadataConfiguration.getExpectedEntityId()).isEqualTo("https://signin.service.gov.uk");
        assertThat(metadataConfiguration.getTrustStorePassword()).startsWith("bj76");
        assertThat(metadataConfiguration.getTrustStorePath()).endsWith(PRODUCTION_VERIFY_TRUSTSTORE_NAME);
        assertThat(metadataConfiguration.getJerseyClientConfiguration()).isNotNull();
        assertThat(metadataConfiguration.getJerseyClientName()).isEqualTo(HUB_JERSEY_CLIENT_NAME);
        assertThat(metadataConfiguration.getMinRefreshDelay()).isEqualTo(60000);
        assertThat(metadataConfiguration.getMaxRefreshDelay()).isEqualTo(600000);
    }

    @Test
    public void shouldSetHubMetadataExpectedEntityIdToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String config = "{" +
            "\"environment\": \"INTEGRATION\"," +
            "\"metadata\": {" +
            "\"entityId\": \"some-expected-entity-id\"" +
            "}}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getExpectedEntityId()).isEqualTo("some-expected-entity-id");
    }

    @Test
    public void shouldSetHubMetadataTrustStorePathToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String config = "{" +
            "\"environment\": \"PRODUCTION\"," +
            "\"metadata\": {" +
            "\"trustStorePath\": \"some-trust-store-path\"" +
            "}}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getTrustStorePath()).isEqualTo("some-trust-store-path");
    }

    @Test
    public void shouldSetHubSsoLocationToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String config = "{" +
            "\"environment\": \"COMPLIANCE_TOOL\"," +
            "\"hubSsoLocation\": \"http://some-hub-sso-location\"" +
            "}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubSsoLocation().toString()).isEqualTo("http://some-hub-sso-location");
    }

    @Test
    public void shouldSetHubMetadataUriToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String config = "{" +
            "\"environment\": \"INTEGRATION\"," +
            "\"metadata\": {" +
            "\"metadataUri\": \"http://some-metadata-location\"" +
            "}}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getUri().toString()).isEqualTo("http://some-metadata-location");
    }
}
