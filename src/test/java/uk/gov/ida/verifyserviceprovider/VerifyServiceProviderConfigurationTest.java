package uk.gov.ida.verifyserviceprovider;

import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static io.dropwizard.jersey.validation.Validators.newValidator;

public class VerifyServiceProviderConfigurationTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    private final YamlConfigurationFactory factory = new YamlConfigurationFactory(
        VerifyServiceProviderConfiguration.class,
        newValidator(),
        newObjectMapper(),
        "dw."
    );

    @Test
    @Ignore
    public void shouldNotComplainWhenConfiguredCorrectly() throws Exception {
        factory.build(
            new FileConfigurationSourceProvider(),
            VerifyServiceProviderConfigurationTest.class.getResource("/verify-service-provider.yml").getPath()
        );
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
    public void shouldNotAllowEmptySecret() throws Exception {
        expectedException.expectMessage("secret may not be empty");
        factory.build(new StringConfigurationSourceProvider("secret: \"\""), "");
    }

    @Test
    public void shouldNotAllowEmptyMsaTrustStore() throws Exception {
        expectedException.expectMessage("msaTrustStore.path may not be empty");
        factory.build(new StringConfigurationSourceProvider("msaTrustStore: \n  path: \"\"\n  password: \"\""), "");
    }

    @Test
    public void shouldNotAllowEmptyHubTrustStore() throws Exception {
        expectedException.expectMessage("hubTrustStore.path may not be empty");
        factory.build(new StringConfigurationSourceProvider("hubTrustStore: \n  path: \"\"\n  password: \"\""), "");
    }

    @Test
    public void shouldNotAllowEmptyRelyingPartyTrustStore() throws Exception {
        expectedException.expectMessage("relyingPartyTrustStore.path may not be empty");
        factory.build(new StringConfigurationSourceProvider("relyingPartyTrustStore: \n  path: \"\"\n  password: \"\""), "");
    }

    @Test
    @Ignore
    public void shouldNotAllowEmptySigningKeys() throws Exception {
        expectedException.expectMessage("signingKeys.primary may not be empty");
        factory.build(new StringConfigurationSourceProvider("signingKeys:\n  primary:\n    publicKey:\"\"\n    privateKey:\"\""), "");
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
