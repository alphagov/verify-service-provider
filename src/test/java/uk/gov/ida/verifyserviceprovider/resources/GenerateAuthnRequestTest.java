package uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class GenerateAuthnRequestTest {

    private static final URI HUB_LOCATION = URI.create("http://example.com");

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new GenerateAuthnRequestResource(getConfiguration()))
            .build();

    private static VerifyServiceProviderConfiguration getConfiguration() {
        class TestVerifyServiceProviderConfiguration extends VerifyServiceProviderConfiguration {
            public TestVerifyServiceProviderConfiguration() {
                this.hubLocation = HUB_LOCATION;
            }
        }

        return new TestVerifyServiceProviderConfiguration();
    }

    @Test
    public void generateAuthnRequest() {
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2);

        Response response = resources.target("/generate-request").request().post(Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        RequestResponseBody requestResponseBody = response.readEntity(RequestResponseBody.class);
        assertThat(requestResponseBody.samlRequest).isNotEmpty();
        assertThat(requestResponseBody.secureToken).isNotEmpty();
        assertThat(requestResponseBody.location).isEqualTo(HUB_LOCATION);
    }
  }
