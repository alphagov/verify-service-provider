package feature.uk.gov.ida.verifyserviceprovider.configuration;

import common.uk.gov.ida.verifyserviceprovider.utils.SystemUtils;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderConfigurationTest;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationConfigurationFeatureTests {

    private DropwizardAppRule<VerifyServiceProviderConfiguration> application;

    @Before
    public void setUp(){
        application = new DropwizardAppRule<>(
            VerifyServiceProviderApplication.class,
            VerifyServiceProviderConfigurationTest.class.getResource("/verify-service-provider-feature-tests.yml").getPath()
        );
    }

    @Test
    public void applicationShouldStartUp() {
        SystemUtils.setEnv(new HashMap<String, String>() {{
            put("HUB_SSO_LOCATION", "some-hub-sso-location");
            put("HUB_ENTITY_ID", "some-hub-entity-id");
            put("MSA_ENTITY_ID", "some-msa-entity-id");
            put("HUB_METADATA_URL", "some-hub-metadata-url");
            put("MSA_METADATA_URL", "some-msa-metadata-url");
            put("SECURE_TOKEN_SEED", "some-secret");
            put("MSA_TRUST_STORE_PATH", "some-msa-trust-store-path");
            put("MSA_TRUST_STORE_PASSWORD", "some-msa-trust-store-password");
            put("HUB_TRUST_STORE_PATH", "some-hub-trust-store-path");
            put("HUB_TRUST_STORE_PASSWORD", "some-hub-trust-store-password");
            put("RELYING_PARTY_TRUST_STORE_PATH", "some-relying-party-trust-store-path");
            put("RELYING_PARTY_TRUST_STORE_PASSWORD", "some-relying-party-trust-store-password");
            put("SIGNING_PRIVATE_KEY", "some-signing-private-key");
            put("ENCRYPTION_CERTIFICATES", "encryption-certificates-1,encryption-certificates-2");
        }});

        application.getTestSupport().before();

        VerifyServiceProviderConfiguration configuration = application.getConfiguration();

        assertThat(configuration.getHubSsoLocation()).isEqualTo("some-hub-sso-location");
        assertThat(configuration.getHubEntityId()).isEqualTo("some-hub-entity-id");
        assertThat(configuration.getMsaEntityId()).isEqualTo("some-msa-entity-id");
        assertThat(configuration.getHubMetadataUrl().toString()).isEqualTo("some-hub-metadata-url");
        assertThat(configuration.getMsaMetadataUrl().toString()).isEqualTo("some-msa-metadata-url");
        assertThat(configuration.getSecureTokenSeed()).isEqualTo("some-secret");
        assertThat(configuration.getMsaTrustStore().getPath()).isEqualTo("some-msa-trust-store-path");
        assertThat(configuration.getMsaTrustStore().getPassword()).isEqualTo("some-msa-trust-store-password");
        assertThat(configuration.getHubTrustStore().getPath()).isEqualTo("some-hub-trust-store-path");
        assertThat(configuration.getHubTrustStore().getPassword()).isEqualTo("some-hub-trust-store-password");
        assertThat(configuration.getRelyingPartyTrustStore().getPath()).isEqualTo("some-relying-party-trust-store-path");
        assertThat(configuration.getRelyingPartyTrustStore().getPassword()).isEqualTo("some-relying-party-trust-store-password");
        assertThat(configuration.getSigningPrivateKey()).isEqualTo("some-signing-private-key");
        assertThat(configuration.getEncryptionCertificates()).contains("encryption-certificates-1","encryption-certificates-2");
    }
}
