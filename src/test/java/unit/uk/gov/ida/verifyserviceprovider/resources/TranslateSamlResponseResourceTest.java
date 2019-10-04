package unit.uk.gov.ida.verifyserviceprovider.resources;

import com.google.common.collect.ImmutableSet;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.event.Level;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.verifyserviceprovider.dto.MatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JerseyViolationExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JsonProcessingExceptionMapper;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseResource;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

@RunWith(MockitoJUnitRunner.class)
public class TranslateSamlResponseResourceTest {

    private static ResponseService responseService = mock(ResponseService.class);
    private static EntityIdService entityIdService = mock(EntityIdService.class);
    private static final String defaultEntityId = "http://default-entity-id";

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addProvider(JerseyViolationExceptionMapper.class)
        .addProvider(JsonProcessingExceptionMapper.class)
        .addProvider(InvalidEntityIdExceptionMapper.class)
        .addResource(new TranslateSamlResponseResource(responseService, entityIdService))
        .build();

    @Before
    public void mockEntityIdService() {
        when(entityIdService.getEntityId(any(TranslateSamlResponseBody.class))).thenReturn(defaultEntityId);
    }

    @After
    public void setup() {
        reset(responseService);
    }

    @Test
    public void shouldUseResponseServiceToTranslateSaml() {
        JSONObject translateResponseRequest = new JSONObject().put("samlResponse", "some-saml-response")
            .put("requestId", "some-request-id")
            .put("levelOfAssurance", LEVEL_2.name());

        when(responseService.convertTranslatedResponseBody(any(), eq("some-request-id"), eq(LEVEL_2), eq(defaultEntityId)))
            .thenReturn(new TranslatedMatchingResponseBody(MatchingScenario.SUCCESS_MATCH, "some-request-id", LEVEL_2, null));

        Response response = resources.client()
            .target("/translate-response")
            .request()
            .post(json(translateResponseRequest.toString()));

        verify(responseService, times(1)).convertTranslatedResponseBody(
            translateResponseRequest.getString("samlResponse"), "some-request-id", LEVEL_2, defaultEntityId
        );
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldReturn400WhenSamlValidationExceptionThrown() {
        JSONObject translateResponseRequest = new JSONObject().put("samlResponse", "some-saml-response")
                .put("requestId", "some-request-id")
                .put("levelOfAssurance", LEVEL_2.name());

        when(responseService.convertTranslatedResponseBody(any(), eq("some-request-id"), eq(LEVEL_2), eq(defaultEntityId)))
                .thenThrow(new SamlResponseValidationException("Some error."));

        Response response = resources.client()
                .target("/translate-response")
                .request()
                .post(json(translateResponseRequest.toString()));

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        ErrorMessage actualError = response.readEntity(ErrorMessage.class);
        assertThat(actualError.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(actualError.getMessage()).isEqualTo("Some error.");
    }

    @Test
    public void shouldReturn400WhenSamlTransformationErrorExceptionThrown() {
        JSONObject translateResponseRequest = new JSONObject().put("samlResponse", "some-saml-response")
                .put("requestId", "some-request-id")
                .put("levelOfAssurance", LEVEL_2.name());

        when(responseService.convertTranslatedResponseBody(any(), eq("some-request-id"), eq(LEVEL_2), eq(defaultEntityId)))
                .thenThrow(new SamlTransformationErrorException("Some error.", Level.ERROR));

        Response response = resources.client()
            .target("/translate-response")
            .request()
            .post(json(translateResponseRequest.toString()));

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        ErrorMessage actualError = response.readEntity(ErrorMessage.class);
        assertThat(actualError.getCode()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(actualError.getMessage()).isEqualTo("Some error.");
    }

    @Test
    public void shouldReturn400WhenCalledWithEmptyJson() {
        Response response = resources.client()
            .target("/translate-response")
            .request()
            .post(json("{}"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);

        ErrorMessage actualErrorMessage = response.readEntity(ErrorMessage.class);
        assertThat(actualErrorMessage.getCode()).isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);

        Set<String> expectedErrors = ImmutableSet.of("requestId may not be null", "samlResponse may not be null", "levelOfAssurance may not be null");
        Set<String> actualErrors = Arrays.stream(actualErrorMessage.getMessage().split(", ")).collect(Collectors.toSet());
        assertThat(actualErrors).isEqualTo(expectedErrors);
    }

}
