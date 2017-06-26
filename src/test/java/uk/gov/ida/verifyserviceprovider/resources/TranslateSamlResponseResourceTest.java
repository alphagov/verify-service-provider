package uk.gov.ida.verifyserviceprovider.resources;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.json.JSONObject;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.Map;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_1;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public class TranslateSamlResponseResourceTest {

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
        .addResource(new TranslateSamlResponseResource())
        .build();

    @Test
    public void translateAuthnResponseWithSuccessfulLOA1Match() {
        Map<String, String> data = ImmutableMap.of(
            "levelOfAssurance", "LEVEL_1",
            "pid", "some-pid-id"
        );

        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(
            getSamlResponseFor(data),
            "secure-token"
        );

        Response response = resources.target("/translate-response").request().post(entity(translateSamlResponseBody, APPLICATION_JSON_TYPE));
        TranslatedResponseBody translatedResponseBody = response.readEntity(TranslatedResponseBody.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(translatedResponseBody.pid).isEqualTo("some-pid-id");
        assertThat(translatedResponseBody.levelOfAssurance).isEqualTo(LEVEL_1);
        assertThat(translatedResponseBody.attributes).isEmpty();
    }

    @Test
    public void translateAuthnResponseWithSuccessfulLOA2Match() {
        Map<String, String> data = ImmutableMap.of(
            "levelOfAssurance", "LEVEL_2",
            "pid", "some-pid-id"
        );

        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(
            getSamlResponseFor(data),
            "secure-token"
        );

        Response response = resources.target("/translate-response").request().post(entity(translateSamlResponseBody, APPLICATION_JSON_TYPE));
        TranslatedResponseBody translatedResponseBody = response.readEntity(TranslatedResponseBody.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(translatedResponseBody.pid).isEqualTo("some-pid-id");
        assertThat(translatedResponseBody.levelOfAssurance).isEqualTo(LEVEL_2);
        assertThat(translatedResponseBody.attributes).isEmpty();
    }

    @Test
    public void translateAuthnResponseShouldReturn400WhenUnknownLevel() {
        Map<String, String> data = ImmutableMap.of(
            "levelOfAssurance", "some-unknown-level",
            "pid", "some-pid-id"
        );

        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(
            getSamlResponseFor(data),
            "secure-token"
        );

        Response response = resources.target("/translate-response")
            .request()
            .post(entity(translateSamlResponseBody, APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
    }

    private String getSamlResponseFor(Map<String, String> data) {
        String samlRequestJson = new JSONObject(data).toString();
        return new String(Base64.getEncoder().encode(samlRequestJson.getBytes()));
    }
}
