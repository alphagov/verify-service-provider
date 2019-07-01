package unit.uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import common.uk.gov.ida.verifyserviceprovider.utils.EnvironmentHelper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.testing.ResourceHelpers;
import org.apache.commons.text.StrSubstitutor;
import org.joda.time.Duration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.verifyserviceprovider.configuration.EuropeanIdentityConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyHubConfiguration;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.exceptions.NoHashingEntityIdIsProvidedError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static io.dropwizard.jersey.validation.Validators.newValidator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;


public class VerifyServiceProviderConfigurationTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    private final YamlConfigurationFactory factory = new YamlConfigurationFactory<>(
        VerifyServiceProviderConfiguration.class,
        newValidator(),
        newObjectMapper(),
        "dw."
    );
    private EnvironmentHelper environmentHelper = new EnvironmentHelper();

    @Test
    public void shouldNotComplainWhenConfiguredCorrectly() throws Exception {
        environmentHelper.setEnv(new HashMap<String, String>() {{
            put("PORT", "50555");
            put("LOG_LEVEL", "ERROR");
            put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL");
            put("MSA_METADATA_URL", "some-msa-metadata-url");
            put("MSA_ENTITY_ID", "some-msa-entity-id");
            put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\"]");
            put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY);
            put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("SAML_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY);
            put("CLOCK_SKEW", "PT30s");
            put("EUROPEAN_IDENTITY_ENABLED", "false");
            put("HUB_CONNECTOR_ENTITY_ID", "etc");
            put("TRUST_ANCHOR_URI", "etc");
            put("METADATA_SOURCE_URI", "etc");
            put("TRUSTSTORE_PATH", "etc");
            put("TRUSTSTORE_PASSWORD", "etc");
        }});

        factory.build(
            new SubstitutingSourceProvider(
                new FileConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)
            ),
                ResourceHelpers.resourceFilePath("verify-service-provider-with-msa.yml")
        );
        environmentHelper.cleanEnv();
    }

    @Test
    public void shouldReturnHashingEntityIdWhenItIsDefined() {

        VerifyServiceProviderConfiguration verifyServiceProviderConfiguration = aVerifyServiceProviderConfiguration(
                Arrays.asList("http://some-service-entity-id","http://some-service-entity-id2"),
                "provided-hashing-entity-id"
        );

        assertThat(verifyServiceProviderConfiguration.getHashingEntityId()).isEqualTo("provided-hashing-entity-id");
    }


    @Test
    public void shouldUseServiceEntityIdForHashingWhenHashingEntityIdNotSpecified(){

        VerifyServiceProviderConfiguration verifyServiceProviderConfiguration = aVerifyServiceProviderConfiguration(
                Collections.singletonList("http://some-service-entity-id"),
                null
        );

        assertThat(verifyServiceProviderConfiguration.getHashingEntityId()).isEqualTo("http://some-service-entity-id");
    }

    @Test
    public void shouldReturnHashingEntityIdWhenOneServiceEntityIdIsProvided() {

        VerifyServiceProviderConfiguration verifyServiceProviderConfiguration = aVerifyServiceProviderConfiguration(
                Collections.singletonList("http://some-service-entity-id"),
                "provided-hashing-entity-id"
        );

        assertThat(verifyServiceProviderConfiguration.getHashingEntityId()).isEqualTo("provided-hashing-entity-id");
    }

    @Test
    public void shouldThrowNoHashingEntityIdIsProvidedErrorWhenMultipleServiceEntityIdsAreProvided() {

        VerifyServiceProviderConfiguration verifyServiceProviderConfiguration = aVerifyServiceProviderConfiguration(
                Arrays.asList("http://some-service-entity-id", "http://some-service-entity-id2"),
                null
        );

        expectedException.expect(NoHashingEntityIdIsProvidedError.class);
        expectedException.expectMessage("No HashingEntityId is provided");

        verifyServiceProviderConfiguration.getHashingEntityId();
    }

    @Test
    public void shouldNotAllowMsaAndEidasConfigTogether() throws Exception {
        Map<String, String> map = ImmutableMap.<String,String>builder()
            .put("PORT", "50555")
            .put("LOG_LEVEL", "ERROR")
            .put("VERIFY_ENVIRONMENT", "COMPLIANCE_TOOL")
            .put("MSA_METADATA_URL", "some-msa-metadata-url")
            .put("MSA_ENTITY_ID", "some-msa-entity-id")
            .put("SERVICE_ENTITY_IDS", "[\"http://some-service-entity-id\"]")
            .put("SAML_SIGNING_KEY", TEST_RP_PRIVATE_SIGNING_KEY)
            .put("SAML_PRIMARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY)
            .put("SAML_SECONDARY_ENCRYPTION_KEY", TEST_RP_PRIVATE_ENCRYPTION_KEY)
            .put("CLOCK_SKEW", "PT30s")
            .put("EUROPEAN_IDENTITY_ENABLED", "false")
            .put("HUB_CONNECTOR_ENTITY_ID", "etc")
            .put("TRUST_ANCHOR_URI", "etc")
            .put("METADATA_SOURCE_URI", "etc")
            .put("TRUSTSTORE_PATH", "etc")
            .put("TRUSTSTORE_PASSWORD", "etc")
            .build();

        String newErrorMessage = "eIDAS and MSA support cannot be set together." +
            " The VSP's eIDAS support is only available when it operates without the MSA";
        assertThatThrownBy(() -> {
                factory.build(
                    new SubstitutingSourceProvider(
                        new FileConfigurationSourceProvider(),
                        new StrSubstitutor(map)
                    ),
                    ResourceHelpers.resourceFilePath("verify-service-provider-with-msa-and-eidas.yml")
                );
            }).isInstanceOf(ConfigurationException.class).hasMessageContaining(newErrorMessage);
    }

    private VerifyServiceProviderConfiguration aVerifyServiceProviderConfiguration(List<String> serviceEntityIds, String hashingEntityId) {
        return new VerifyServiceProviderConfiguration(
                serviceEntityIds,
                hashingEntityId,
                mock(VerifyHubConfiguration.class),
                mock(PrivateKey.class),
                mock(PrivateKey.class),
                mock(PrivateKey.class),
                Optional.empty(),
                new Duration(1000L),
                Optional.ofNullable(mock(EuropeanIdentityConfiguration.class))
        );
    }


    @Test
    public void shouldNotAllowNullValues() throws Exception {
        expectedException.expectMessage("server may not be null");

        factory.build(new StringConfigurationSourceProvider("server: "), "");
    }

    @Test
    public void shouldNotAllowEmptySamlSigningKey() throws Exception {
        expectedException.expectMessage("Failed to parse configuration at: samlSigningKey");
        factory.build(new StringConfigurationSourceProvider("samlSigningKey: \"\""), "");
    }

    @Test
    public void shouldNotAllowEmptySamlPrimaryEncryptionKey() throws Exception {
        expectedException.expectMessage("Failed to parse configuration at: samlPrimaryEncryptionKey");
        factory.build(new StringConfigurationSourceProvider("samlPrimaryEncryptionKey: \"\""), "");
    }

    class StringConfigurationSourceProvider implements ConfigurationSourceProvider {

        private String configuration;

        public StringConfigurationSourceProvider(String configuration) {
            this.configuration = configuration;
        }

        @Override
        public InputStream open(String path) throws IOException {
            return new ByteArrayInputStream(this.configuration.getBytes());
        }
    }
}
