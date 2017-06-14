package uk.gov.ida.verifyserviceprovider.resources;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslateResponseRequestBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class TranslateAuthnResponseTest {

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new TranslateResponseResource())
            .build();

    @Test
    public void translateAuthnResponseWithSuccessfulLOA1Match() {
        TranslateResponseRequestBody translateResponseRequestBody = new TranslateResponseRequestBody("saml-response-LEVEL_1", "secure-token");

        Response response = resources.target("/translate-response").request().post(Entity.entity(translateResponseRequestBody, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        TranslatedResponseBody translatedResponseBody = response.readEntity(TranslatedResponseBody.class);
        assertThat(translatedResponseBody.pid).isNotEmpty();
        assertThat(translatedResponseBody.levelOfAssurance).isEqualTo(LevelOfAssurance.LEVEL_1);
        assertThat(translatedResponseBody.attributes).isEmpty();
    }

    @Test
    public void translateAuthnResponseWithSuccessfulLOA2Match() {
        TranslateResponseRequestBody translateResponseRequestBody = new TranslateResponseRequestBody("saml-response-LEVEL_2", "secure-token");

        Response response = resources.target("/translate-response").request().post(Entity.entity(translateResponseRequestBody, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        TranslatedResponseBody translatedResponseBody = response.readEntity(TranslatedResponseBody.class);
        assertThat(translatedResponseBody.pid).isNotEmpty();
        assertThat(translatedResponseBody.levelOfAssurance).isEqualTo(LevelOfAssurance.LEVEL_2);
        assertThat(translatedResponseBody.attributes).isEmpty();
    }
}
