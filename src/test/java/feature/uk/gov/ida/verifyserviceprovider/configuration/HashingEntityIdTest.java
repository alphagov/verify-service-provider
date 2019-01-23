package feature.uk.gov.ida.verifyserviceprovider.configuration;

import common.uk.gov.ida.verifyserviceprovider.utils.EnvironmentHelper;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.KeyStoreResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.exceptions.NoHashingEntityIdIsProvidedError;

import java.util.HashMap;

import static keystore.builders.KeyStoreResourceBuilder.aKeyStoreResource;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

public class HashingEntityIdTest {

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
            "verify-service-provider-no-hashing-entity-id.yml",
            ConfigOverride.config("logging.loggers.uk\\.gov", "DEBUG"),
            ConfigOverride.config("verifyHubConfiguration.metadata.trustStore.path", keyStoreResource.getAbsolutePath()),
            ConfigOverride.config("verifyHubConfiguration.metadata.trustStore.password", keyStoreResource.getPassword()),
            ConfigOverride.config("europeanIdentity.enabled", "false"),
            ConfigOverride.config("europeanIdentity.hubConnectorEntityId", "dummyEntity"),
            ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustAnchorUri", "http://dummy.com"),
            ConfigOverride.config("europeanIdentity.aggregatedMetadata.metadataSourceUri", "http://dummy.com"),
            ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustStore.path", keyStoreResource.getAbsolutePath()),
            ConfigOverride.config("europeanIdentity.aggregatedMetadata.trustStore.password", keyStoreResource.getPassword())
        );
    }

    @After
    public void cleanup() {
        application.getTestSupport().after();
        environmentHelper.cleanEnv();
    }

    @Test
    public void applicationShouldStartUpWithSingleServiceEntityIdIfNoHashingEntityIdProvided() throws Exception {
        environmentHelper.setEnv(new HashMap<String, String>() {{
            put("PORT", "50555");
            put("LOG_LEVEL", "ERROR");
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\"]");
            put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("SAML_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("CLOCK_SKEW", "PT30s");
        }});

        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(configuration.getHashingEntityId()).isEqualTo("http://some-service-entity-id");
    }

    @Test(expected = NoHashingEntityIdIsProvidedError.class)
    public void applicationShouldFailToStartWithMultipleServiceEntityIdsIfNoHashingEntityIdProvided() throws Exception {
        environmentHelper.setEnv(new HashMap<String, String>() {{
            put("PORT", "50555");
            put("LOG_LEVEL", "ERROR");
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\",\"http://some-other-service-entity-id\"]");
            put("HASHING_ENTITY_ID", "some-hashing-entity-id");
            put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("SAML_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("CLOCK_SKEW", "PT30s");
        }});


        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(configuration.getHashingEntityId()).isEqualTo("some-hashing-entity-id");
    }

}
