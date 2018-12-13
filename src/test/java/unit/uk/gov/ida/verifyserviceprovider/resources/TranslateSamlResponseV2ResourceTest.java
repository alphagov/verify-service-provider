package unit.uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedNonMatchingResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JerseyViolationExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JsonProcessingExceptionMapper;
import uk.gov.ida.verifyserviceprovider.resources.TranslateSamlResponseV2Resource;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
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
public class TranslateSamlResponseV2ResourceTest {

    private static ResponseService responseService = mock(ResponseService.class);
    private static EntityIdService entityIdService = mock(EntityIdService.class);
    private static final String defaultEntityId = "http://default-entity-id";

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(JerseyViolationExceptionMapper.class)
            .addProvider(JsonProcessingExceptionMapper.class)
            .addProvider(InvalidEntityIdExceptionMapper.class)
            .addResource(new TranslateSamlResponseV2Resource(responseService, entityIdService))
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
    public void shouldHaveAPostEndpoint() throws Exception {

        Response response = resources.client()
                .target("/translate-non-matching-response")
                .request()
                .post(json(""));

        assertThat(response.getStatus()).isNotEqualTo(NOT_FOUND.getStatusCode());
    }

    @Test
    public void shouldUseResponseServiceToTranslateSaml() throws Exception {
        JSONObject translateResponseRequest = new JSONObject().put("samlResponse", "some-saml-response")
            .put("requestId", "some-request-id")
            .put("levelOfAssurance", LEVEL_2.name());

        when(responseService.convertTranslatedResponseBody(any(), eq("some-request-id"), eq(LEVEL_2), eq(defaultEntityId)))
            .thenReturn(new TranslatedNonMatchingResponseBody(NonMatchingScenario.IDENTITY_VERIFIED, "some-request-id", LEVEL_2, null));

        Response response = resources.client()
            .target("/translate-non-matching-response")
            .request()
            .post(json(translateResponseRequest.toString()));

        verify(responseService, times(1)).convertTranslatedResponseBody(
            translateResponseRequest.getString("samlResponse"), "some-request-id", LEVEL_2, defaultEntityId
        );
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}