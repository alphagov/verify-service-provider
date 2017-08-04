package unit.uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.json.JSONObject;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.verifyserviceprovider.dto.ErrorBody;
import uk.gov.ida.verifyserviceprovider.exceptions.SamlResponseValidationException;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseResource;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TranslateSamlResponseResourceTest {

    private static ResponseService responseService = mock(ResponseService.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addResource(new TranslateSamlResponseResource(responseService))
        .build();

    @Test
    public void shouldUseResponseServiceToTranslateSaml() throws Exception {
        JSONObject translateResponseRequest = new JSONObject().put("samlResponse", "some-saml-response");

        Response response = resources.client()
            .target("/translate-response")
            .request()
            .post(json(translateResponseRequest.toString()));

        verify(responseService, times(1)).convertTranslatedResponseBody(translateResponseRequest.getString("samlResponse"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void shouldReturn400WhenSamlValidationExceptionThrown() throws Exception {
        JSONObject translateResponseRequest = new JSONObject().put("samlResponse", "some-saml-response");

        when(responseService.convertTranslatedResponseBody(any())).thenThrow(new SamlResponseValidationException("Some error."));

        Response response = resources.client()
                .target("/translate-response")
                .request()
                .post(json(translateResponseRequest.toString()));

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        ErrorBody actualError = response.readEntity(ErrorBody.class);
        assertThat(actualError.getReason()).isEqualTo(BAD_REQUEST.name());
        assertThat(actualError.getMessage()).isEqualTo("Some error.");
    }

}
