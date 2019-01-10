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

    private static final KeyStoreResource KEY_STORE = KeyStoreResourceBuilder.aKeyStoreResource()
            .withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();

    @ClassRule
    public static final EnvironmentVariables ENVIRONMENT_VARIABLES = new EnvironmentVariables();

    @Before
    public void setUp() {
        KEY_STORE.create();
    }

    @After
    public void tearDown() {
        KEY_STORE.delete();
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
    public void shouldSetConfigurationValuesWhenOverridesAreProvided() throws Exception {
        String config = "{\"environment\": \"CUSTOM\"}";
        setUpEnvironmentForCustom();

        VerifyHubConfiguration actualConfiguration = OBJECT_MAPPER.readValue(config, VerifyHubConfiguration.class);

        assertThat(actualConfiguration.getHubMetadataConfiguration().getExpectedEntityId()).isEqualTo("http://some-expected-entity-id");
        assertThat(actualConfiguration.getHubMetadataConfiguration().getTrustStore().containsAlias("rootCA")).isTrue();
        assertThat(actualConfiguration.getHubSsoLocation().toString()).isEqualTo("http://some-sso-url");
        assertThat(actualConfiguration.getHubMetadataConfiguration().getUri().toString()).isEqualTo("http://some-metadata-url");
    }

    private void setUpEnvironmentForCustom() {
        ENVIRONMENT_VARIABLES.set(HUB_SSO_URL, "http://some-sso-url");
        ENVIRONMENT_VARIABLES.set(HUB_METADATA_URL, "http://some-metadata-url");
        ENVIRONMENT_VARIABLES.set(HUB_EXPECTED_ENTITY_ID, "http://some-expected-entity-id");
        ENVIRONMENT_VARIABLES.set(METADATA_TRUSTSTORE_PATH, KEY_STORE.getAbsolutePath());
        ENVIRONMENT_VARIABLES.set(HUB_TRUSTSTORE_PATH, KEY_STORE.getAbsolutePath());
        ENVIRONMENT_VARIABLES.set(IDP_TRUSTSTORE_PATH, KEY_STORE.getAbsolutePath());
        ENVIRONMENT_VARIABLES.set(TRUSTSTORE_PASSWORD, KEY_STORE.getPassword());
    }
}
