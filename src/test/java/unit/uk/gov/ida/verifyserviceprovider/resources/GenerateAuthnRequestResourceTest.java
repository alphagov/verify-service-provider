package unit.uk.gov.ida.verifyserviceprovider.resources;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JerseyViolationExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JsonProcessingExceptionMapper;
import uk.gov.ida.verifyserviceprovider.factories.saml.AuthnRequestFactory;
import uk.gov.ida.verifyserviceprovider.logging.AuthnRequestAttributesHelper;
import uk.gov.ida.verifyserviceprovider.logging.AuthnRequestAttributesHelper.AuthnRequestAttibuteNames;
import uk.gov.ida.verifyserviceprovider.resources.GenerateAuthnRequestResource;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GenerateAuthnRequestResourceTest {

    private static final String GENERATE_REQUEST_RESOURCE_PATH = "/generate-request";
    private static final URI HUB_SSO_LOCATION = URI.create("http://example.com/SAML2/SSO");
    private static final String DEFAULT_ENTITY_ID = "http://default-entity-id";
    private static final String TEST_REQUEST_ID = "request_id";
    private static final String TEST_DESTINATION = "http://acme.service/authnRequest";
    private static final String TEST_ISSUE_INSTANT = "2015-04-30T19:25:14.273Z";
    private static final String TEST_ISSUER = "http://acme.service";
    private static final String AUTHN_REQUEST_ATTRIBUTES_LOG_MESSAGE = "AuthnRequest Attributes:";

    private static AuthnRequestFactory authnRequestFactory = mock(AuthnRequestFactory.class);
    private static EntityIdService entityIdService = mock(EntityIdService.class);

    private AuthnRequest authnRequest;

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(JerseyViolationExceptionMapper.class)
            .addProvider(JsonProcessingExceptionMapper.class)
            .addProvider(InvalidEntityIdExceptionMapper.class)
            .addResource(new GenerateAuthnRequestResource(authnRequestFactory, HUB_SSO_LOCATION, entityIdService))
            .build();

    @Before
    public void mockAuthnRequestFactory() {
        authnRequest = new AuthnRequestBuilder().buildObject();
        authnRequest.setID(TEST_REQUEST_ID);
        authnRequest.setDestination(TEST_DESTINATION);
        authnRequest.setIssueInstant(DateTime.parse(TEST_ISSUE_INSTANT));
        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue(TEST_ISSUER);
        authnRequest.setIssuer(issuer);
        reset(authnRequestFactory);
    }

    @Before
    public void bootStrapOpenSaml() {
        IdaSamlBootstrap.bootstrap();
    }

    @Before
    public void mockEntityIdService() {
        when(entityIdService.getEntityId(any(RequestGenerationBody.class))).thenReturn(DEFAULT_ENTITY_ID);
    }

    @Test
    public void returnsAnOKResponseWithoutLoaParam() {
        when(authnRequestFactory.build(any())).thenReturn(authnRequest);
        Response response = resources.target(GENERATE_REQUEST_RESOURCE_PATH).request().post(Entity.entity(ImmutableMap.of(), MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void returnsAnOKResponseWhenNoParams() {
        when(authnRequestFactory.build(any())).thenReturn(authnRequest);
        Response response = resources.target(GENERATE_REQUEST_RESOURCE_PATH).request().post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void returnsAnOKResponse() {
        when(authnRequestFactory.build(any())).thenReturn(authnRequest);
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2, null);

        Response response = resources.target(GENERATE_REQUEST_RESOURCE_PATH).request().post(Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void responseContainsExpectedFields() {
        when(authnRequestFactory.build(eq(DEFAULT_ENTITY_ID))).thenReturn(authnRequest);
        RequestResponseBody requestResponseBody = generateRequest();
        assertThat(requestResponseBody.getSamlRequest()).isNotEmpty();
        assertThat(requestResponseBody.getRequestId()).isNotEmpty();
        assertThat(requestResponseBody.getSsoLocation()).isNotNull();
    }

    @Test
    public void ssoLocationIsSameAsConfiguration() {
        when(authnRequestFactory.build(eq(DEFAULT_ENTITY_ID))).thenReturn(authnRequest);
        RequestResponseBody requestResponseBody = generateRequest();
        assertThat(requestResponseBody.getSsoLocation()).isEqualTo(HUB_SSO_LOCATION);
    }

    @Test
    public void samlRequestIsBase64EncodedAuthnRequest() {
        when(authnRequestFactory.build(eq(DEFAULT_ENTITY_ID))).thenReturn(authnRequest);
        RequestResponseBody requestResponseBody = generateRequest();
        try {
            Base64.getDecoder().decode(requestResponseBody.getSamlRequest());
        } catch (IllegalArgumentException e) {
            Assertions.fail("Expected samlRequest to be Base64 encoded");
        }
    }

    @Test
    public void returns422ForBadJson() {
        Response response = resources.target(GENERATE_REQUEST_RESOURCE_PATH)
                .request()
                .post(Entity.entity(ImmutableMap.of("bad", "json"), MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(422);
        ErrorMessage errorMessage = response.readEntity(ErrorMessage.class);
        assertThat(errorMessage.getCode()).isEqualTo(422);
        assertThat(errorMessage.getMessage()).startsWith("Unrecognized field \"bad\"");
    }

    @Test
    public void returns422ForMalformedJson() {
        Response response = resources.target(GENERATE_REQUEST_RESOURCE_PATH)
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
        when(authnRequestFactory.build(any())).thenThrow(RuntimeException.class);

        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2, null);
        Response response = resources.target(GENERATE_REQUEST_RESOURCE_PATH).request().post(Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE));

        ErrorMessage responseEntity = response.readEntity(ErrorMessage.class);
        assertThat(responseEntity.getCode()).isEqualTo(500);
        assertThat(responseEntity.getMessage()).contains("There was an error processing your request. It has been logged (ID");
        assertThat(responseEntity.getDetails()).isNullOrEmpty();
    }

    @Test
    public void shouldLogAuthnRequestAttributesToMDC() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(AuthnRequestAttributesHelper.class);
        Appender<ILoggingEvent> appender = mock(Appender.class);
        logger.addAppender(appender);

        when(authnRequestFactory.build(any())).thenReturn(authnRequest);
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2, null);

        Response response =
                resources.target(GENERATE_REQUEST_RESOURCE_PATH).request().post(
                        Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE)
                );
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender).doAppend(loggingEventArgumentCaptor.capture());

        ILoggingEvent loggingEvent = loggingEventArgumentCaptor.getValue();
        Map<String, String> mdcPropertyMap = loggingEvent.getMDCPropertyMap();


        assertThat(loggingEvent.getMessage()).contains(AUTHN_REQUEST_ATTRIBUTES_LOG_MESSAGE);

        assertThat(mdcPropertyMap.get(AuthnRequestAttibuteNames.REQUEST_ID)).isEqualTo(TEST_REQUEST_ID);
        assertThat(mdcPropertyMap.get(AuthnRequestAttibuteNames.DESTINATION)).isEqualTo(TEST_DESTINATION);
        assertThat(mdcPropertyMap.get(AuthnRequestAttibuteNames.ISSUE_INSTANT)).isEqualTo(TEST_ISSUE_INSTANT);
        assertThat(mdcPropertyMap.get(AuthnRequestAttibuteNames.ISSUER)).isEqualTo(TEST_ISSUER);
    }

    @Test
    public void shouldLeaveMDCInPreviousStateAfterLogging() throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(AuthnRequestAttributesHelper.class);
        Appender<ILoggingEvent> appender = mock(Appender.class);
        logger.addAppender(appender);

        // Log and then Check state of MDC before calling the resource
        MDC.put("testMDCKey", "testMDCValue");
        logger.info("Do some logging to see what's on the MDC beforehand...");

        ArgumentCaptor<ILoggingEvent> loggingEventArgumentCaptor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender).doAppend(loggingEventArgumentCaptor.capture());

        int mdcPropertyMapSizeBeforeCall = loggingEventArgumentCaptor.getValue().getMDCPropertyMap().size();

        when(authnRequestFactory.build(any())).thenReturn(authnRequest);
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2, null);

        Response response =
                resources.target(GENERATE_REQUEST_RESOURCE_PATH).request().post(
                        Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE)
                );

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(appender, Mockito.times(2)).doAppend(loggingEventArgumentCaptor.capture());

        // Resource should have added 4 items and then removed them from the MDC, leaving anything that was there previously
        assertThat(loggingEventArgumentCaptor.getValue().getMDCPropertyMap().size()).isEqualTo(mdcPropertyMapSizeBeforeCall + 4);
        assertThat(MDC.get("testMDCKey")).isEqualTo("testMDCValue");

    }

    private RequestResponseBody generateRequest() {
        RequestGenerationBody requestGenerationBody = new RequestGenerationBody(LevelOfAssurance.LEVEL_2, null);
        Response response = resources.target(GENERATE_REQUEST_RESOURCE_PATH).request().post(Entity.entity(requestGenerationBody, MediaType.APPLICATION_JSON_TYPE));
        return response.readEntity(RequestResponseBody.class);
    }
}
