package feature.uk.gov.ida.verifyserviceprovider.configuration;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderApplication;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;

import javax.ws.rs.core.Response;
import java.net.URI;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckFeatureTest {

    @ClassRule
    public static DropwizardAppRule<VerifyServiceProviderConfiguration> application = new DropwizardAppRule<>(
            VerifyServiceProviderApplication.class,
            resourceFilePath("verify-service-provider.yml")
    );

    @Test
    public void shouldProvideHealthCheckEndpoint() {
        Response response = application.client()
                .target(URI.create(String.format("http://localhost:%d/admin/healthcheck", application.getLocalPort())))
                .request()
                .buildGet()
                .invoke();

        String expectedResult = "{\"applicationHealthCheck\":{\"healthy\":true},\"deadlocks\":{\"healthy\":true}}";

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(String.class)).isEqualTo(expectedResult);
    }
}
