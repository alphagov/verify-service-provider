package unit.uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.resources.GenerateAuthnRequestResource;
import uk.gov.ida.verifyserviceprovider.saml.AuthnRequestFactory;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenerateAuthnRequestTest {

    private static final URI HUB_SSO_LOCATION = URI.create("http://example.com/SAML2/SSO");

    private static AuthnRequestFactory authnRequestFactory = Mockito.mock(AuthnRequestFactory.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addResource(new GenerateAuthnRequestResource(authnRequestFactory, HUB_SSO_LOCATION))
        .build();

    @Before
    public void mockAuthnRequestFactory() {
        AuthnRequest authnRequest = new AuthnRequestBuilder().buildObject();
        authnRequest.setID("some-id");
        when(authnRequestFactory.build(Mockito.any())).thenReturn(authnRequest);
    }

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void returnsAnOKResponse() {
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2);

        Response response = resources.target("/generate-request").request().post(Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void responseContainsExpectedFields() {
        RequestResponseBody requestResponseBody = generateRequest();
        assertThat(requestResponseBody.getSamlRequest()).isNotEmpty();
        assertThat(requestResponseBody.getRequestId()).isNotEmpty();
        assertThat(requestResponseBody.getSsoLocation()).isNotNull();
    }

    @Test
    public void ssoLocationIsSameAsConfiguration() {
        RequestResponseBody requestResponseBody = generateRequest();
        assertThat(requestResponseBody.getSsoLocation()).isEqualTo(HUB_SSO_LOCATION);
    }

    @Test
    public void samlRequestIsBase64EncodedAuthnRequest() {
        RequestResponseBody requestResponseBody = generateRequest();
        try {
            Base64.getDecoder().decode(requestResponseBody.getSamlRequest());
        } catch(IllegalArgumentException e) {
            Assertions.fail("Expected samlRequest to be Base64 encoded");
        }
    }

    private RequestResponseBody generateRequest() {
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2);
        Response response = resources.target("/generate-request").request().post(Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE));
        return response.readEntity(RequestResponseBody.class);
    }
}
