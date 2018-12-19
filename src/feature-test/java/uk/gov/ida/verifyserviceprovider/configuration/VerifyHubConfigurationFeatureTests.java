package uk.gov.ida.verifyserviceprovider.configuration;

import certificates.values.CACertificates;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_EXPECTED_ENTITY_ID;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_METADATA_URL;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_SSO_URL;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.IDP_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.METADATA_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.TRUSTSTORE_PASSWORD;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.HUB_JERSEY_CLIENT_NAME;
import static uk.gov.ida.verifyserviceprovider.utils.DefaultObjectMapper.OBJECT_MAPPER;

public class VerifyHubConfigurationFeatureTests {

    private static final KeyStoreResource keyStore = KeyStoreResourceBuilder.aKeyStoreResource()
            .withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    @ClassRule
    public static final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Before
    public void setUp() {
        keyStore.create();
    }

    @After
    public void tearDown() {
        keyStore.delete();
    }

    @Test
    public void shouldSetExpectedDefaultsForComplianceToolEnvironmentIfNoOverridesGiven() throws Exception {
        String config = "{\"environment\": \"COMPLIANCE_TOOL\"}";

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);
        MetadataResolverConfiguration metadataConfiguration = actualConfiguration.getHubMetadataConfiguration();

        assertThat(actualConfiguration.getHubSsoLocation().toString()).isEqualTo("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/SSO");
        assertThat(metadataConfiguration.getUri().toString()).isEqualTo("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/federation");
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
        MetadataResolverConfiguration metadataConfiguration = actualConfiguration.getHubMetadataConfiguration();

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
        MetadataResolverConfiguration metadataConfiguration = actualConfiguration.getHubMetadataConfiguration();

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
        String config = "{\"environment\": \"CUSTOM\"}";
        setUpEnvironmentForCustom();

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getExpectedEntityId()).isEqualTo("http://some-expected-entity-id");
    }

    @Test
    public void shouldSetHubMetadataTrustStorePathToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String config = "{\"environment\": \"CUSTOM\"}";
        setUpEnvironmentForCustom();
        environmentVariables.set(METADATA_TRUSTSTORE_PATH, keyStore.getAbsolutePath());
        environmentVariables.set(TRUSTSTORE_PASSWORD, keyStore.getPassword());

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getTrustStore().containsAlias("rootCA")).isTrue();
    }

    @Test
    public void shouldSetHubSsoLocationToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String config = "{\"environment\": \"CUSTOM\"}";
        setUpEnvironmentForCustom();

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubSsoLocation().toString()).isEqualTo("http://some-sso-url");
    }

    @Test
    public void shouldSetHubMetadataUriToTheConfigValueIfOneHasBeenProvided() throws Exception {
        String config = "{\"environment\": \"CUSTOM\"}";
        setUpEnvironmentForCustom();

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getUri().toString()).isEqualTo("http://some-metadata-url");
    }

    private void setUpEnvironmentForCustom() {
        environmentVariables.set(HUB_SSO_URL, "http://some-sso-url");
        environmentVariables.set(HUB_METADATA_URL, "http://some-metadata-url");
        environmentVariables.set(HUB_EXPECTED_ENTITY_ID, "http://some-expected-entity-id");
        environmentVariables.set(METADATA_TRUSTSTORE_PATH, keyStore.getAbsolutePath());
        environmentVariables.set(HUB_TRUSTSTORE_PATH, keyStore.getAbsolutePath());
        environmentVariables.set(IDP_TRUSTSTORE_PATH, keyStore.getAbsolutePath());
        environmentVariables.set(TRUSTSTORE_PASSWORD, keyStore.getPassword());
    }
}
