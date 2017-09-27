package unit.uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.jersey.errors.ErrorMessage;
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
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JerseyViolationExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JsonProcessingExceptionMapper;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;
import uk.gov.ida.verifyserviceprovider.resources.GenerateAuthnRequestResource;
import uk.gov.ida.verifyserviceprovider.utils.ServiceEntityIdHelper;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenerateAuthnRequestTest {

    private static final URI HUB_SSO_LOCATION = URI.create("http://example.com/SAML2/SSO");
    private static final String defaultEntityId = "http://default-entity-id";

    private static AuthnRequestFactory authnRequestFactory = mock(AuthnRequestFactory.class);
    private static ServiceEntityIdHelper serviceEntityIdHelper = mock(ServiceEntityIdHelper.class);

    private AuthnRequest authnRequest;

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addProvider(JerseyViolationExceptionMapper.class)
        .addProvider(JsonProcessingExceptionMapper.class)
        .addProvider(InvalidEntityIdExceptionMapper.class)
        .addResource(new GenerateAuthnRequestResource(authnRequestFactory, HUB_SSO_LOCATION, serviceEntityIdHelper))
        .build();

    @Before
    public void mockAuthnRequestFactory() {
        authnRequest = new AuthnRequestBuilder().buildObject();
        authnRequest.setID("some-id");
        reset(authnRequestFactory);
    }

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Before
    public void mockServiceEntityIdHelper() {
        when(serviceEntityIdHelper.getEntityId(any(RequestGenerationBody.class))).thenReturn(defaultEntityId);
    }

    @Test
    public void returnsAnOKResponse() {
        when(authnRequestFactory.build(any(), any())).thenReturn(authnRequest);
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2, null);

        Response response = resources.target("/generate-request").request().post(Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void responseContainsExpectedFields() {
        when(authnRequestFactory.build(any(), eq(defaultEntityId))).thenReturn(authnRequest);
        RequestResponseBody requestResponseBody = generateRequest();
        assertThat(requestResponseBody.getSamlRequest()).isNotEmpty();
        assertThat(requestResponseBody.getRequestId()).isNotEmpty();
        assertThat(requestResponseBody.getSsoLocation()).isNotNull();
    }

    @Test
    public void ssoLocationIsSameAsConfiguration() {
        when(authnRequestFactory.build(any(), eq(defaultEntityId))).thenReturn(authnRequest);
        RequestResponseBody requestResponseBody = generateRequest();
        assertThat(requestResponseBody.getSsoLocation()).isEqualTo(HUB_SSO_LOCATION);
    }

    @Test
    public void samlRequestIsBase64EncodedAuthnRequest() {
        when(authnRequestFactory.build(any(), eq(defaultEntityId))).thenReturn(authnRequest);
        RequestResponseBody requestResponseBody = generateRequest();
        try {
            Base64.getDecoder().decode(requestResponseBody.getSamlRequest());
        } catch (IllegalArgumentException e) {
            Assertions.fail("Expected samlRequest to be Base64 encoded");
        }
    }

    @Test
    public void returns422ForBadJson() {
        Response response = resources.target("/generate-request")
            .request()
            .post(Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(ErrorMessage.class)).isEqualTo(new ErrorMessage(
            422,
            "levelOfAssurance may not be null")
        );
    }

    @Test
    public void returns422ForMalformedJson() {
        Response response = resources.target("/generate-request")
            .request()
            .post(Entity.entity("this is not json", MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(ErrorMessage.class)).isEqualTo(new ErrorMessage(
            422,
            "Unrecognized token 'this': was expecting 'null', 'true', 'false' or NaN")
        );
    }

    @Test
    public void returns500IfARuntimeExceptionIsThrown() {
        when(authnRequestFactory.build(any(), any())).thenThrow(RuntimeException.class);

        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2, null);
        Response response = resources.target("/generate-request").request().post(Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE));

        ErrorMessage responseEntity = response.readEntity(ErrorMessage.class);
        assertThat(responseEntity.getCode()).isEqualTo(500);
        assertThat(responseEntity.getMessage()).contains("There was an error processing your request. It has been logged (ID");
        assertThat(responseEntity.getDetails()).isNullOrEmpty();
    }

    private RequestResponseBody generateRequest() {
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2, null);
        Response response = resources.target("/generate-request").request().post(Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE));
        return response.readEntity(RequestResponseBody.class);
    }
}
