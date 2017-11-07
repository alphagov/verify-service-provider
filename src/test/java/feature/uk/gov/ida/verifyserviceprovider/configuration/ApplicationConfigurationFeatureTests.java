package feature.uk.gov.ida.verifyserviceprovider.configuration;

import common.uk.gov.ida.verifyserviceprovider.utils.EnvironmentHelper;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.KeyStoreResource;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.HubEnvironment;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import java.util.HashMap;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static org.apache.xml.security.utils.Base64.decode;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class ApplicationConfigurationFeatureTests {

    private DropwizardAppRule<VerifyServiceProviderConfiguration> application;
    private EnvironmentHelper environmentHelper = new EnvironmentHelper();

    @Before
    public void setUp() {
        KeyStoreResource keyStoreResource = aKeyStoreResource()
            .withCertificate("any-alias", aCertificate().build().getCertificate())
            .build();
        keyStoreResource.create();
        application = new DropwizardAppRule<>(
            VerifyServiceProviderApplication.class,
            resourceFilePath("verify-service-provider.yml"),
            ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
            ConfigOverride.config("verifyHubConfiguration.metadata.trustStorePath", keyStoreResource.getAbsolutePath()),
            ConfigOverride.config("verifyHubConfiguration.metadata.trustStorePassword", keyStoreResource.getPassword())
        );
    }

    @After
    public void cleanup() {
        application.getTestSupport().after();
        environmentHelper.cleanEnv();
    }

    @Test
    public void applicationShouldStartUp() throws Exception {
        environmentHelper.setEnv(new HashMap<String, String>() {{
            put("PORT", "50555");
            put("LOG_LEVEL", "ERROR");
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("MSA_METADATA_URL", "some-msa-metadata-url");
            put("MSA_ENTITY_ID", "some-msa-entity-id");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\"]");
            put("VSP_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("VSP_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("VSP_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("CLOCK_SKEW", "PT5s");
        }});

        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(application.getLocalPort()).isEqualTo(50555);
        assertThat(((DefaultLoggingFactory) configuration.getLoggingFactory()).getLevel().toString()).isEqualTo("ERROR");
        assertThat(configuration.getHubSsoLocation().toString()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getSsoLocation().toString());
        assertThat(configuration.getVerifyHubMetadata().getUri().toString()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getMetadataUri().toString());
        assertThat(configuration.getVerifyHubMetadata().getExpectedEntityId()).isEqualTo("https://signin.service.gov.uk");
        assertThat(configuration.getMatchingServiceAdapter().getEntityId()).isEqualTo("some-msa-entity-id");
        assertThat(configuration.getMatchingServiceAdapter().getMetadataUri().toString()).isEqualTo("some-msa-metadata-url");
        assertThat(configuration.getServiceEntityIds()).containsExactly("http://some-service-entity-id");
        assertThat(configuration.getSigningKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_SIGNING_KEY));
        assertThat(configuration.getPrimaryEncryptionKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        assertThat(configuration.getSecondaryEncryptionKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        assertThat(configuration.getClockSkew()).isEqualTo(Duration.standardSeconds(5));
    }

    @Test
    public void applicationShouldStartUpWithListOfServiceEntityIds() throws Exception {
        environmentHelper.setEnv(new HashMap<String, String>() {{
            put("PORT", "50555");
            put("LOG_LEVEL", "ERROR");
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("MSA_METADATA_URL", "some-msa-metadata-url");
            put("MSA_ENTITY_ID", "some-msa-entity-id");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\",\"http://some-other-service-entity-id\"]");
            put("VSP_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("VSP_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("VSP_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("CLOCK_SKEW", "PT5s");
        }});

        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(application.getLocalPort()).isEqualTo(50555);
        assertThat(((DefaultLoggingFactory) configuration.getLoggingFactory()).getLevel().toString()).isEqualTo("ERROR");
        assertThat(configuration.getHubSsoLocation().toString()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getSsoLocation().toString());
        assertThat(configuration.getVerifyHubMetadata().getUri().toString()).isEqualTo(HubEnvironment.COMPLIANCE_TOOL.getMetadataUri().toString());
        assertThat(configuration.getVerifyHubMetadata().getExpectedEntityId()).isEqualTo("https://signin.service.gov.uk");
        assertThat(configuration.getMatchingServiceAdapter().getEntityId()).isEqualTo("some-msa-entity-id");
        assertThat(configuration.getMatchingServiceAdapter().getMetadataUri().toString()).isEqualTo("some-msa-metadata-url");
        assertThat(configuration.getServiceEntityIds()).containsExactly("http://some-service-entity-id", "http://some-other-service-entity-id");
        assertThat(configuration.getSigningKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_SIGNING_KEY));
        assertThat(configuration.getPrimaryEncryptionKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        assertThat(configuration.getSecondaryEncryptionKey().getEncoded()).isEqualTo(decode(TEST_RP_PRIVATE_ENCRYPTION_KEY));
        assertThat(configuration.getClockSkew()).isEqualTo(Duration.standardSeconds(5));
    }
}
