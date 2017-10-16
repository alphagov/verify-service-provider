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
        expectedException.expectMessage(containsString("signingKey may not be null"));
        expectedException.expectMessage(containsString("primaryEncryptionKey may not be null"));
        expectedException.expectMessage(containsString("verifyHubConfiguration may not be null"));
        expectedException.expectMessage(containsString("matchingServiceAdapter may not be null"));

        factory.build(new StringConfigurationSourceProvider("server: "), "");
    }

    @Test
    public void shouldNotAllowEmptySamlSigningKey() throws Exception {
        expectedException.expectMessage("Failed to parse configuration at: signingKey");
        factory.build(new StringConfigurationSourceProvider("signingKey: \"\""), "");
    }

    @Test
    public void shouldNotAllowEmptySamlPrimaryEncryptionKey() throws Exception {
        expectedException.expectMessage("Failed to parse configuration at: primaryEncryptionKey");
        factory.build(new StringConfigurationSourceProvider("primaryEncryptionKey: \"\""), "");
    }

    @Test
    public void shouldNotAllowEmptyMatchingServiceAdapterEntityId() throws Exception {
        expectedException.expectMessage("matchingServiceAdapter.entityId may not be null");
        factory.build(new StringConfigurationSourceProvider("matchingServiceAdapter: \n metadataUri: https://some-url.com"), "");
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
