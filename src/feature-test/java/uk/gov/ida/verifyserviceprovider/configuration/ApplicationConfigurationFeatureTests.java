package uk.gov.ida.verifyserviceprovider.configuration;

import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.KeyStoreResource;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;

import java.net.URI;
import java.util.HashMap;

import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static org.apache.xml.security.utils.Base64.decode;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_EXPECTED_ENTITY_ID;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_METADATA_URL;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_SSO_URL;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.HUB_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.IDP_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.METADATA_TRUSTSTORE_PATH;
import static uk.gov.ida.verifyserviceprovider.configuration.ConfigurationConstants.EnvironmentVariables.TRUSTSTORE_PASSWORD;

public class ApplicationConfigurationFeatureTests {

    private DropwizardAppRule<VerifyServiceProviderConfiguration> application;

    @Rule
    public EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Before
    public void setUp() {
        application = new DropwizardAppRule<>(
            VerifyServiceProviderApplication.class,
            "verify-service-provider.yml"
        );
        new HashMap<String, String>() {{
            put("PORT", "50555");
            put("LOG_LEVEL", "ERROR");
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("MSA_METADATA_URL", "some-msa-metadata-url");
            put("MSA_ENTITY_ID", "some-msa-entity-id");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\"]");
            put("HASHING_ENTITY_ID", "some-hashing-entity-id");
            put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("SAML_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("CLOCK_SKEW", "PT30s");
        }}.forEach(environmentVariables::set);
    }

    @After
    public void cleanup() {
        application.getTestSupport().after();
    }

    @Test
    public void applicationShouldStartUp() throws Exception {
        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(application.getLocalPort()).isEqualTo(50555);
        assertThat(((DefaultLoggingFactory) configuration.getLoggingFactory()).getLevel()).isEqualTo("ERROR");
        assertThat(configuration.getHubSsoLocation().toString()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getSsoLocation().toString());
        assertThat(configuration.getHubMetadataConfiguration().getUri().toString()).isEqualTo("https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/metadata/federation");
        assertThat(configuration.getHubMetadataConfiguration().getExpectedEntityId()).isEqualTo("https://signin.service.gov.uk");
        assertThat(configuration.getMsaMetadataConfiguration().getExpectedEntityId()).isEqualTo("some-msa-entity-id");
        assertThat(configuration.getMsaMetadataConfiguration().getUri().toString()).isEqualTo("some-msa-metadata-url");
        assertThat(configuration.getServiceEntityIds()).containsExactly("http://some-service-entity-id");
        assertThat(configuration.getSamlSigningKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_SIGNING_KEY));
        assertThat(configuration.getSamlPrimaryEncryptionKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        assertThat(configuration.getSamlSecondaryEncryptionKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        assertThat(configuration.getClockSkew()).isEqualTo(Duration.standardSeconds(30));
    }

    @Test
    public void applicationShouldStartUpWithListOfServiceEntityIds() throws NoSuchFieldException, IllegalAccessException {
        environmentVariables.set("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\",\"http://some-other-service-entity-id\"]");
        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(configuration.getServiceEntityIds()).containsExactly("http://some-service-entity-id", "http://some-other-service-entity-id");
        assertThat(configuration.getHashingEntityId()).isEqualTo("some-hashing-entity-id");
    }

    @Test
    public void applicationShouldUseOverriddenHubMetadataValuesWhenUsingCustomConfiguration() {
        KeyStoreResource keyStoreResource = aKeyStoreResource()
                .withCertificate("any-alias", aCertificate().build().getCertificate())
                .build();
        keyStoreResource.create();
        URI ssoUri = URI.create("http://test.com/SAML2/SSO");
        URI metadataUri = URI.create("http://test.com/SAML2/metadata");
        String expectedEntityId = "https://test.entity.id";

        application = new DropwizardAppRule<>(
                VerifyServiceProviderApplication.class,
                "verify-service-provider.yml"
        );

        new HashMap<String, String>() {{
            put("VERIFY_ENVIRONMENT", "CUSTOM");
            put(HUB_SSO_URL, ssoUri.toString());
            put(HUB_METADATA_URL, metadataUri.toString());
            put(HUB_EXPECTED_ENTITY_ID, expectedEntityId);
            put(METADATA_TRUSTSTORE_PATH, keyStoreResource.getAbsolutePath());
            put(HUB_TRUSTSTORE_PATH, keyStoreResource.getAbsolutePath());
            put(IDP_TRUSTSTORE_PATH, keyStoreResource.getAbsolutePath());
            put(TRUSTSTORE_PASSWORD, keyStoreResource.getPassword());
        }}.forEach(environmentVariables::set);

        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(configuration.getHubSsoLocation()).isEqualTo(ssoUri);
        assertThat(configuration.getHubMetadataConfiguration().getUri()).isEqualTo(metadataUri);
        assertThat(configuration.getHubMetadataConfiguration().getExpectedEntityId()).isEqualTo(expectedEntityId);
    }
}
