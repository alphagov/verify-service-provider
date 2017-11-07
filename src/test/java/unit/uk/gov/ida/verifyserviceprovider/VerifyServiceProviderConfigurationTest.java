package unit.uk.gov.ida.verifyserviceprovider;

import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static io.dropwizard.jersey.validation.Validators.newValidator;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.core.StringContains.containsString;

public class VerifyServiceProviderConfigurationTest {

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
        factory.build(
            new SubstitutingSourceProvider(
                new FileConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)
            ),
            resourceFilePath("verify-service-provider.yml")
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
