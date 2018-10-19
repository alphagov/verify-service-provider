package unit.uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.exceptions.InvalidEntityIdExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JerseyViolationExceptionMapper;
import uk.gov.ida.verifyserviceprovider.exceptions.JsonProcessingExceptionMapper;
import uk.gov.ida.verifyserviceprovider.resources.TranslateNonMatchingSamlResponseResource;
import uk.gov.ida.verifyserviceprovider.services.EntityIdService;
import uk.gov.ida.verifyserviceprovider.services.ResponseService;

import javax.ws.rs.core.Response;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TranslateNonMatchingSamlResponseResourceTest {

    private static ResponseService responseService = mock(ResponseService.class);
    private static EntityIdService entityIdService = mock(EntityIdService.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(JerseyViolationExceptionMapper.class)
            .addProvider(JsonProcessingExceptionMapper.class)
            .addProvider(InvalidEntityIdExceptionMapper.class)
            .addResource(new TranslateNonMatchingSamlResponseResource(responseService, entityIdService))
            .build();

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
}