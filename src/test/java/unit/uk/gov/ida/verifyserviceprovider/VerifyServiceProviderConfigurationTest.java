package unit.uk.gov.ida.verifyserviceprovider;

import common.uk.gov.ida.verifyserviceprovider.utils.EnvironmentHelper;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static io.dropwizard.jersey.validation.Validators.newValidator;
import static org.hamcrest.core.StringContains.containsString;
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

    @After
    public void after() {
        environmentHelper.cleanEnv();
    }

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
        }});

        factory.build(
            new SubstitutingSourceProvider(
                new FileConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)
            ),
            "verify-service-provider.yml"
        );
    }

    @Test
    public void shouldNotAllowNullValues() throws Exception {
        expectedException.expectMessage(containsString("server may not be null"));
        expectedException.expectMessage(containsString("serviceEntityIds may not be null"));
        expectedException.expectMessage(containsString("samlSigningKey may not be null"));
        expectedException.expectMessage(containsString("samlPrimaryEncryptionKey may not be null"));
        expectedException.expectMessage(containsString("verifyHubConfiguration may not be null"));
        expectedException.expectMessage(containsString("msaMetadata may not be null"));

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
