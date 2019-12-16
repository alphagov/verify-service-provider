package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import io.dropwizard.jersey.errors.ErrorMessage;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppRule;
import uk.gov.ida.verifyserviceprovider.services.ComplianceToolService;
import uk.gov.ida.verifyserviceprovider.services.GenerateRequestService;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.verifyserviceprovider.builders.ComplianceToolV1InitialisationRequestBuilder.aComplianceToolV1InitialisationRequest;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_1;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.dto.MatchingScenario.ACCOUNT_CREATION;
import static uk.gov.ida.verifyserviceprovider.services.ComplianceToolService.ACCOUNT_CREATION_LOA1_ID;
import static uk.gov.ida.verifyserviceprovider.services.ComplianceToolService.ACCOUNT_CREATION_LOA2_ID;

public class UserAccountCreationResponseAcceptanceTest {

    @ClassRule
    public static MockMsaServer msaServer = new MockMsaServer();

    @ClassRule
    public static VerifyServiceProviderAppRule application = new VerifyServiceProviderAppRule(msaServer);

    private static Client client;
    private static ComplianceToolService complianceTool;
    private static GenerateRequestService generateRequestService;

    @BeforeClass
    public static void setUpBeforeClass() {
        client = application.client();
        complianceTool = new ComplianceToolService(client);
        generateRequestService = new GenerateRequestService(client);
    }

    @Test
    public void shouldHandleAUserAccountCreationResponse() {
        Response response = getResponse(LEVEL_2,
            "FIRST_NAME",
            "FIRST_NAME_VERIFIED",
            "DATE_OF_BIRTH",
            "DATE_OF_BIRTH_VERIFIED",
            "CURRENT_ADDRESS",
            "CURRENT_ADDRESS_VERIFIED",
            "ADDRESS_HISTORY",
            "CYCLE_3");
        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        assertThat(jsonResponse.getString("scenario")).isEqualTo(ACCOUNT_CREATION.name());
        assertThat(jsonResponse.getString("pid")).isEqualTo("some-expected-pid");
        assertThat(jsonResponse.keySet()).contains("attributes");
        assertThat(jsonResponse.getString("levelOfAssurance")).isEqualTo(LEVEL_2.name());
    }

    @Test
    public void shouldHandleLoA1UserAccountCreationResponse() {
        Response response = getResponse(LEVEL_1,
                "FIRST_NAME",
                "FIRST_NAME_VERIFIED",
                "DATE_OF_BIRTH",
                "DATE_OF_BIRTH_VERIFIED",
                "CURRENT_ADDRESS",
                "CURRENT_ADDRESS_VERIFIED",
                "ADDRESS_HISTORY",
                "CYCLE_3");
        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        assertThat(jsonResponse.getString("scenario")).isEqualTo(ACCOUNT_CREATION.name());
        assertThat(jsonResponse.getString("pid")).isEqualTo("some-expected-pid");
        assertThat(jsonResponse.keySet()).contains("attributes");
        assertThat(jsonResponse.getString("levelOfAssurance")).isEqualTo(LEVEL_1.name());
    }

    @Test
    public void shouldOnlyReturnRequestedAttributes() {
        Response response = getResponse(LEVEL_2, "FIRST_NAME", "FIRST_NAME_VERIFIED", "DATE_OF_BIRTH", "DATE_OF_BIRTH_VERIFIED");
        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        JSONObject attributes = jsonResponse.getJSONObject("attributes");

        assertThat(attributes.keySet()).containsExactly("firstName", "dateOfBirth");
    }

    @Test
    public void shouldUseISO8601DateFormat() {
        Response response = getResponse(LEVEL_2, "DATE_OF_BIRTH", "DATE_OF_BIRTH_VERIFIED");

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        JSONObject attributes = jsonResponse.getJSONObject("attributes");

        String dob = attributes.getJSONObject("dateOfBirth").getString("value");
        assertThat(dob).matches(Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$"));
    }

    @Test
    public void shouldErrorIfOnlyVerifiedIsRequested() {
        Response response = getResponse(LEVEL_2, "DATE_OF_BIRTH_VERIFIED");
        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        ErrorMessage errorResponse = response.readEntity(ErrorMessage.class);

        assertThat(errorResponse.getMessage()).isEqualTo("Invalid attributes request: Cannot request verification status without requesting attribute value");
    }

    @Test
    public void shouldErrorIfVerifiedIsNotRequested() {
        Response response = getResponse(LEVEL_2, "SURNAME");
        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());

        ErrorMessage errorResponse = response.readEntity(ErrorMessage.class);

        assertThat(errorResponse.getMessage()).isEqualTo("Invalid attributes request: Cannot request attribute without requesting verification status. Please check your MSA configuration settings.");
    }

    private Response getResponse(LevelOfAssurance levelOfAssurance, String... attributes) {
        complianceTool.initialiseWith(
            aComplianceToolV1InitialisationRequest()
                .withMatchingServiceSigningPrivateKey(TEST_RP_MS_PRIVATE_SIGNING_KEY)
                .withMatchingServiceEntityId(MockMsaServer.MSA_ENTITY_ID)
                .withEncryptionCertificate(TEST_RP_PUBLIC_ENCRYPTION_CERT)
                .withExpectedPid("some-expected-pid")
                .withUserAccountCreationAttributes(Arrays.asList(attributes))
                .build()
        );

        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());

        int testCaseId = levelOfAssurance == LEVEL_2 ? ACCOUNT_CREATION_LOA2_ID : ACCOUNT_CREATION_LOA1_ID;
        Map<String, String> translateResponseRequestData = ImmutableMap.of(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), testCaseId),
            "requestId", requestResponseBody.getRequestId(),
            "levelOfAssurance", levelOfAssurance.name()
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        return response;
    }
}
