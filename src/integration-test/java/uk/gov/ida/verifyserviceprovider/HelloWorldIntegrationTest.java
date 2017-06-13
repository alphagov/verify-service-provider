package uk.gov.ida.verifyserviceprovider;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class HelloWorldIntegrationTest {

    @ClassRule
    public static DropwizardAppRule<VerifyServiceProviderConfiguration> helloWorldAppRule = new DropwizardAppRule<>(
            VerifyServiceProviderApplication.class, ResourceHelpers.resourceFilePath("verify-service-provider.yml"));

    public Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);

    @Test
    public void sayHello() {
        Response response = client.target(UriBuilder.fromUri("http://localhost")
                .path("/hello-world")
                .port(helloWorldAppRule.getLocalPort())
                .build()).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.readEntity(String.class)).isEqualTo("Hello World");
    }

}
