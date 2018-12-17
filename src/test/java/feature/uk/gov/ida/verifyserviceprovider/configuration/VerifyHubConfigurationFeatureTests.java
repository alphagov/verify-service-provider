package feature.uk.gov.ida.verifyserviceprovider.configuration;

import certificates.values.CACertificates;
import common.uk.gov.ida.verifyserviceprovider.utils.EnvironmentHelper;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.saml.metadata.MetadataResolverConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyHubConfiguration;

import java.util.HashMap;

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

@Ignore(value = "TODO: Implement CUSTOM environment")
public class VerifyHubConfigurationFeatureTests {

    private static final KeyStoreResource keyStore = KeyStoreResourceBuilder.aKeyStoreResource()
            .withCertificate("rootCA", CACertificates.TEST_ROOT_CA).build();
    private EnvironmentHelper environmentHelper = new EnvironmentHelper();

    @Before
    public void setUp() {
        keyStore.create();
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
        environmentHelper.put(METADATA_TRUSTSTORE_PATH, keyStore.getAbsolutePath());
        environmentHelper.put(TRUSTSTORE_PASSWORD, keyStore.getPassword());

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
        HashMap<String, String> env = new HashMap<>();
        env.put(HUB_SSO_URL, "http://some-sso-url");
        env.put(HUB_METADATA_URL, "http://some-metadata-url");
        env.put(HUB_EXPECTED_ENTITY_ID, "http://some-expected-entity-id");
        env.put(METADATA_TRUSTSTORE_PATH, keyStore.getAbsolutePath());
        env.put(HUB_TRUSTSTORE_PATH, keyStore.getAbsolutePath());
        env.put(IDP_TRUSTSTORE_PATH, keyStore.getAbsolutePath());
        env.put(TRUSTSTORE_PASSWORD, keyStore.getPassword());
        environmentHelper.setEnv(env);
    }
}
