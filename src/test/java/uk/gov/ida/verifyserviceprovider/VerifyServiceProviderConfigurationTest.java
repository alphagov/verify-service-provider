package uk.gov.ida.verifyserviceprovider;

import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.testing.ResourceHelpers;
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
            new FileConfigurationSourceProvider(),
            resourceFilePath("verify-service-provider.yml")
        );
    }

    @Test
    public void shouldNotAllowNullValues() throws Exception {
        expectedException.expectMessage(containsString("server may not be null"));
        expectedException.expectMessage(containsString("hubSsoLocation may not be null"));
        expectedException.expectMessage(containsString("hubEntityId may not be null"));
        expectedException.expectMessage(containsString("msaEntityId may not be null"));
        expectedException.expectMessage(containsString("hubMetadataUrl may not be null"));
        expectedException.expectMessage(containsString("msaMetadataUrl may not be null"));
        expectedException.expectMessage(containsString("secureTokenKey may not be null"));
        expectedException.expectMessage(containsString("samlSigningKey may not be null"));
        expectedException.expectMessage(containsString("samlPrimaryEncryptionKey may not be null"));

        factory.build(new StringConfigurationSourceProvider("server: "), "");
    }

    @Test
    public void shouldNotAllowEmptyHubSSOLocation() throws Exception {
        expectedException.expectMessage("hubSsoLocation may not be empty");
        factory.build(new StringConfigurationSourceProvider("hubSsoLocation: \"\""), "");
    }

    @Test
    public void shouldNotAllowEmptyHubEntityId() throws Exception {
        expectedException.expectMessage("hubEntityId may not be empty");
        factory.build(new StringConfigurationSourceProvider("hubEntityId: \"\""), "");
    }

    @Test
    public void shouldNotAllowEmptyMsaEntityId() throws Exception {
        expectedException.expectMessage("msaEntityId may not be empty");
        factory.build(new StringConfigurationSourceProvider("msaEntityId: \"\""), "");
    }

    @Test
    public void shouldNotAllowEmptySecureTokenKey() throws Exception {
        expectedException.expectMessage("secureTokenKey may not be empty");
        factory.build(new StringConfigurationSourceProvider("secureTokenKey: \"\""), "");
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
