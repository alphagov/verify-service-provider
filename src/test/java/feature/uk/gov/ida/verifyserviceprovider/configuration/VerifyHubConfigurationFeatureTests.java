package feature.uk.gov.ida.verifyserviceprovider.configuration;

import certificates.values.CACertificates;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.HubMetadataConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyHubConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.utils.DefaultObjectMapper.OBJECT_MAPPER;

public class VerifyHubConfigurationFeatureTests {

    private static final KeyStoreResource keyStore = KeyStoreResourceBuilder.aKeyStoreResource()
            .withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    @Before
    public void setUp() {
        keyStore.create();
    }

    @Test
    public void shouldSetExpectedDefaultsForComplianceToolEnvironmentIfNoOverridesGiven() throws Exception {
        String config = "{\"environment\": \"COMPLIANCE_TOOL\"}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);
        HubMetadataConfiguration metadataConfiguration = actualConfiguration.getHubMetadataConfiguration();

        assertThat(actualConfiguration.getHubSsoLocation().toString()).isEqualTo("https://compliance-tool-integration.cloudapps.digital/SAML2/SSO");
        assertThat(metadataConfiguration.getUri().toString()).isEqualTo("https://compliance-tool-integration.cloudapps.digital/SAML2/metadata/federation");
        assertThat(metadataConfiguration.getExpectedEntityId()).isEqualTo("https://signin.service.gov.uk");
        assertThat(metadataConfiguration.getTrustStore().containsAlias("idaca")).isTrue();
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
        assertThat(metadataConfiguration.getTrustStore().containsAlias("idaca")).isTrue();
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
        assertThat(metadataConfiguration.getTrustStore().containsAlias("idaca")).isTrue();
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
            "\"expectedEntityId\": \"some-expected-entity-id\"" +
            "}}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getExpectedEntityId()).isEqualTo("some-expected-entity-id");
    }

    @Test
    public void shouldSetHubMetadataTrustStorePathToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String config = "{" +
            "\"environment\": \"PRODUCTION\"," +
            "\"metadata\": {" +
            "\"hubTrustStore\": {" +
            "\"path\": \"" + keyStore.getAbsolutePath() + "\"," +
            "\"password\": \"" + keyStore.getPassword() + "\"" +
            "}}}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getHubTrustStore()).isPresent();
        assertThat(actualConfiguration.getHubMetadataConfiguration().getHubTrustStore().get().containsAlias("rootCA"));
    }

    @Test
    public void shouldSetIdpMetadataTrustStorePathToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String config = "{" +
                "\"environment\": \"PRODUCTION\"," +
                "\"metadata\": {" +
                "\"idpTrustStore\": {" +
                "\"path\": \"" + keyStore.getAbsolutePath() + "\"," +
                "\"password\": \"" + keyStore.getPassword() + "\"" +
                "}}}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getIdpTrustStore()).isPresent();
        assertThat(actualConfiguration.getHubMetadataConfiguration().getIdpTrustStore().get().containsAlias("rootCA"));
    }

    @Test
    public void shouldSetMetadataTrustStorePathToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String config = "{" +
                "\"environment\": \"PRODUCTION\"," +
                "\"metadata\": {" +
                "\"trustStore\": {" +
                "\"path\": \"" + keyStore.getAbsolutePath() + "\"," +
                "\"password\": \"" + keyStore.getPassword() + "\"" +
                "}}}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getTrustStore().containsAlias("rootCA")).isTrue();
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
            "\"uri\": \"http://some-metadata-location\"" +
            "}}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getUri().toString()).isEqualTo("http://some-metadata-location");
    }
}
