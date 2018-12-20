package uk.gov.ida.verifyserviceprovider.configuration;

import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import org.joda.time.Duration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.rules.ExpectedException;
import uk.gov.ida.verifyserviceprovider.exceptions.NoHashingEntityIdIsProvidedError;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static io.dropwizard.jersey.validation.Validators.newValidator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;


public class VerifyServiceProviderConfigurationTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    private final YamlConfigurationFactory factory = new YamlConfigurationFactory<>(
        VerifyServiceProviderConfiguration.class,
        newValidator(),
        newObjectMapper(),
        "dw."
    );

    @Test
    public void shouldNotComplainWhenConfiguredCorrectly() throws Exception {
        new HashMap<String, String>() {{
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
        }}.forEach(environmentVariables::set);

        factory.build(
            new SubstitutingSourceProvider(
                new FileConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)
            ),
            "verify-service-provider.yml"
        );
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

    private VerifyServiceProviderConfiguration aVerifyServiceProviderConfiguration(List<String> serviceEntityIds, String hashingEntityId) {
        return new VerifyServiceProviderConfiguration(
                serviceEntityIds,
                hashingEntityId,
                mock(VerifyHubConfiguration.class),
                mock(PrivateKey.class),
                mock(PrivateKey.class),
                mock(PrivateKey.class),
                mock(MsaMetadataConfiguration.class),
                new Duration(1000L)
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
