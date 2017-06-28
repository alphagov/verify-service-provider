package uk.gov.ida.verifyserviceprovider.resources;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.dto.Address;
import uk.gov.ida.verifyserviceprovider.dto.Attributes;
import uk.gov.ida.verifyserviceprovider.dto.TranslateSamlResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
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
        Map<String, Object> data = ImmutableMap.of(
            "responseType", "some-response-type",
            "levelOfAssurance", "LEVEL_1",
            "pid", "some-pid"
        );

        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(
            getSamlResponseFor(data),
            "secure-token"
        );

        Response response = resources.target("/translate-response").request().post(entity(translateSamlResponseBody, APPLICATION_JSON_TYPE));
        TranslatedResponseBody result = response.readEntity(TranslatedResponseBody.class);

        TranslatedResponseBody expected = new TranslatedResponseBody(
            "some-response-type",
            "some-pid",
            LEVEL_1,
            Optional.empty()
        );

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void translateAuthnResponseWithSuccessfulLOA2Match() {
        Map<String, Object> data = ImmutableMap.of(
            "responseType", "some-response-type",
            "levelOfAssurance", "LEVEL_2",
            "pid", "some-pid"
        );

        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(
            getSamlResponseFor(data),
            "secure-token"
        );

        Response response = resources.target("/translate-response").request().post(entity(translateSamlResponseBody, APPLICATION_JSON_TYPE));
        TranslatedResponseBody result = response.readEntity(TranslatedResponseBody.class);

        TranslatedResponseBody exptected = new TranslatedResponseBody(
            "some-response-type",
            "some-pid",
            LEVEL_2,
            Optional.empty()
        );

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(result).isEqualTo(exptected);
    }

    @Test
    public void translateAuthnResponseWithNoMatch() {
        JSONObject address = new JSONObject()
            .put("verified", true)
            .put("lines", new JSONArray().put("address-line-1").put("address-line-2"))
            .put("postCode", "some-post-code")
            .put("internationalPostCode", "some-international-post-code")
            .put("uprn", "some-uprn")
            .put("fromDate", "2010-01-31")
            .put("toDate", "2017-01-31");

        JSONObject attributes = new JSONObject()
            .put("firstName", "some-first-name")
            .put("firstNameVerified", true)
            .put("middleName", "some-middle-name")
            .put("middleNameVerified", false)
            .put("surname", "some-surname")
            .put("surnameVerified", true)
            .put("dateOfBirth", "2000-01-31")
            .put("dateOfBirthVerified", true)
            .put("address", address)
            .put("cycle3", "some-cycle3");

        JSONObject data = new JSONObject()
            .put("responseType", "some-response-type")
            .put("pid", "some-pid")
            .put("levelOfAssurance", "LEVEL_1")
            .put("attributes", attributes);

        TranslateSamlResponseBody translateSamlResponseBody = new TranslateSamlResponseBody(
            encode(data.toString()),
            "secure-token"
        );

        Response response = resources.target("/translate-response").request().post(entity(translateSamlResponseBody, APPLICATION_JSON_TYPE));
        TranslatedResponseBody result = response.readEntity(TranslatedResponseBody.class);

        Address expectedAddress = new Address(
            true,
            ImmutableList.of("address-line-1", "address-line-2"),
            "some-post-code",
            "some-international-post-code",
            "some-uprn",
            LocalDate.of(2010, 1, 31),
            LocalDate.of(2017, 1, 31)
        );
        Attributes expectedAttributes = new Attributes(
            "some-first-name",
            true,
            "some-middle-name",
            false,
            "some-surname",
            true,
            LocalDate.of(2000, 1, 31),
            true,
            expectedAddress,
            "some-cycle3"
        );
        TranslatedResponseBody expected = new TranslatedResponseBody(
            "some-response-type",
            "some-pid",
            LEVEL_1,
            Optional.of(expectedAttributes)
        );

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void translateAuthnResponseShouldReturn400WhenUnknownLevel() {
        Map<String, Object> data = ImmutableMap.of(
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

    private String getSamlResponseFor(Map<String, Object> data) {
        String samlRequestJson = new JSONObject(data).toString();
        return encode(samlRequestJson);
    }

    private String encode(String content) {
        return new String(Base64.getEncoder().encode(content.getBytes()));
    }
}
