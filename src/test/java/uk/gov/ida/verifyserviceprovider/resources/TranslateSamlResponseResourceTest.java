package uk.gov.ida.verifyserviceprovider.resources;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.json.JSONObject;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TranslateSamlResponseResourceTest {

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new TranslateSamlResponseResource())
            .build();

    @Test
    public void translateAuthnResponseWithSuccessfulLOA1Match() {
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(
            getSamlResponseForLOA("LEVEL_1"),
            "secure-token"
        );

        Response response = resources.target("/translate-response").request().post(Entity.entity(translateSamlResponseBody, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        TranslatedResponseBody translatedResponseBody = response.readEntity(TranslatedResponseBody.class);
        assertThat(translatedResponseBody.pid).isNotEmpty();
        assertThat(translatedResponseBody.levelOfAssurance).isEqualTo(LevelOfAssurance.LEVEL_1);
        assertThat(translatedResponseBody.attributes).isEmpty();
    }

    @Test
    public void translateAuthnResponseWithSuccessfulLOA2Match() {
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(
            getSamlResponseForLOA("LEVEL_2"),
            "secure-token"
        );

        Response response = resources.target("/translate-response").request().post(Entity.entity(translateSamlResponseBody, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        TranslatedResponseBody translatedResponseBody = response.readEntity(TranslatedResponseBody.class);
        assertThat(translatedResponseBody.pid).isNotEmpty();
        assertThat(translatedResponseBody.levelOfAssurance).isEqualTo(LevelOfAssurance.LEVEL_2);
        assertThat(translatedResponseBody.attributes).isEmpty();
    }

    @Test
    public void translateAuthnResponseShouldReturn400WhenUnknownLevel() {
        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(
            getSamlResponseForLOA("some-unknown-level"),
            "secure-token"
        );

        Response response = resources.target("/translate-response").request().post(Entity.entity(translateSamlResponseBody, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    private String getSamlResponseForLOA(String levelOfAssurance) {
        Map<String, String> samlResponseData = ImmutableMap.of(
            "levelOfAssurance", levelOfAssurance,
            "pid", "some-pid-value"
        );

        String samlRequestJson = new JSONObject(samlResponseData).toString();
        return new String(Base64.getEncoder().encode(samlRequestJson.getBytes()));
    }
}
