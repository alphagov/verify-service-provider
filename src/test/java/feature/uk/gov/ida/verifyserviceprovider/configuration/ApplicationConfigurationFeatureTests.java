package feature.uk.gov.ida.verifyserviceprovider.configuration;

import common.uk.gov.ida.verifyserviceprovider.utils.SystemUtils;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderConfigurationTest;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import java.security.PrivateKey;
import java.util.Base64;
import java.util.HashMap;

import static common.uk.gov.ida.verifyserviceprovider.utils.CertAndKeys.generate;
import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationConfigurationFeatureTests {

    private DropwizardAppRule<VerifyServiceProviderConfiguration> application;

    @Before
    public void setUp() {
        application = new DropwizardAppRule<>(
            VerifyServiceProviderApplication.class,
            VerifyServiceProviderConfigurationTest.class.getResource("/verify-service-provider-feature-tests.yml").getPath()
        );
    }

    @Test
    public void applicationShouldStartUp() throws Exception {
        PrivateKey privateKey = generate().privateKey;
        SystemUtils.setEnv(new HashMap<String, String>() {{
            put("PORT", "50555");
            put("HUB_SSO_LOCATION", "some-hub-sso-location");
            put("HUB_ENTITY_ID", "some-hub-entity-id");
            put("MSA_ENTITY_ID", "some-msa-entity-id");
            put("HUB_METADATA_URL", "some-hub-metadata-url");
            put("MSA_METADATA_URL", "some-msa-metadata-url");
            put("SECURE_TOKEN_SEED", "some-secret");
            put("SIGNING_PRIVATE_KEY", new String(Base64.getEncoder().encode(privateKey.getEncoded())));
            put("DECRYPTION_PRIVATE_KEYS", "some-decryption-private-keys-1, some-decryption-private-keys-2");
        }});

        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(application.getLocalPort()).isEqualTo(50555);
        assertThat(configuration.getHubSsoLocation()).isEqualTo("some-hub-sso-location");
        assertThat(configuration.getHubEntityId()).isEqualTo("some-hub-entity-id");
        assertThat(configuration.getMsaEntityId()).isEqualTo("some-msa-entity-id");
        assertThat(configuration.getHubMetadataUrl().toString()).isEqualTo("some-hub-metadata-url");
        assertThat(configuration.getMsaMetadataUrl().toString()).isEqualTo("some-msa-metadata-url");
        assertThat(configuration.getSecureTokenSeed()).isEqualTo("some-secret");
        assertThat(configuration.getSigningPrivateKey().getEncoded()).isEqualTo(privateKey.getEncoded());
        assertThat(configuration.getDecryptionPrivateKeys()).contains("some-decryption-private-keys-1", "some-decryption-private-keys-2");
    }
}
