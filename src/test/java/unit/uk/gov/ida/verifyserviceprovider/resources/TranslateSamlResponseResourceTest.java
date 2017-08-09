package unit.uk.gov.ida.verifyserviceprovider.resources;

import com.google.common.collect.ImmutableSet;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.apache.http.HttpStatus;
import org.apache.log4j.Level;
import org.json.JSONObject;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.verifyserviceprovider.dto.ErrorBody;
import uk.gov.ida.verifyserviceprovider.exceptions.JerseyViolationExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseResource;
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

@RunWith(MockitoJUnitRunner.class)
public class TranslateSamlResponseResourceTest {

    private static ResponseService responseService = mock(ResponseService.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addProvider(JerseyViolationExceptionMapper.class)
        .addResource(new TranslateSamlResponseResource(responseService))
        .build();

    @After
    public void setup() {
        reset(responseService);
    }

    @Test
    public void shouldUseResponseServiceToTranslateSaml() throws Exception {
        JSONObject translateResponseRequest = new JSONObject().put("samlResponse", "some-saml-response").put("requestId", "some-request-id");

        Response response = resources.client()
            .target("/translate-response")
            .request()
            .post(json(translateResponseRequest.toString()));

        verify(responseService, times(1)).convertTranslatedResponseBody(translateResponseRequest.getString("samlResponse"), "some-request-id");
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldReturn400WhenSamlValidationExceptionThrown() throws Exception {
        JSONObject translateResponseRequest = new JSONObject().put("samlResponse", "some-saml-response").put("requestId", "some-request-id");

        when(responseService.convertTranslatedResponseBody(any(), eq("some-request-id"))).thenThrow(new SamlResponseValidationException("Some error."));

        Response response = resources.client()
                .target("/translate-response")
                .request()
                .post(json(translateResponseRequest.toString()));

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        ErrorBody actualError = response.readEntity(ErrorBody.class);
        assertThat(actualError.getReason()).isEqualTo(BAD_REQUEST.name());
        assertThat(actualError.getMessage()).isEqualTo("Some error.");
    }

    @Test
    public void shouldReturn400WhenSamlTransformationErrorExceptionThrown() throws Exception {
        JSONObject translateResponseRequest = new JSONObject().put("samlResponse", "some-saml-response").put("requestId", "some-request-id");

        when(responseService.convertTranslatedResponseBody(any(), eq("some-request-id"))).thenThrow(new SamlTransformationErrorException("Some error.", Level.ERROR));

        Response response = resources.client()
            .target("/translate-response")
            .request()
            .post(json(translateResponseRequest.toString()));

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        ErrorBody actualError = response.readEntity(ErrorBody.class);
        assertThat(actualError.getReason()).isEqualTo(BAD_REQUEST.name());
        assertThat(actualError.getMessage()).isEqualTo("Some error.");
    }

    @Test
    public void shouldReturn400WhenCalledWithEmptyJson() throws Exception {
        Response response = resources.client()
            .target("/translate-response")
            .request()
            .post(json("{}"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_UNPROCESSABLE_ENTITY);

        ErrorBody actualErrorBody = response.readEntity(ErrorBody.class);
        assertThat(actualErrorBody.getReason()).isEqualTo(String.valueOf(HttpStatus.SC_UNPROCESSABLE_ENTITY));

        Set<String> expectedErrors = ImmutableSet.of("requestId may not be null", "samlResponse may not be null");
        Set<String> actualErrors = Arrays.stream(actualErrorBody.getMessage().split(", ")).collect(Collectors.toSet());
        assertThat(actualErrors).isEqualTo(expectedErrors);
    }

}
