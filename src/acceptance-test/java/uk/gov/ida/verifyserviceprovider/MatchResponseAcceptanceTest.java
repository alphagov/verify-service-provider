package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.verifyserviceprovider.builders.ComplianceToolInitialisationRequestBuilder.aComplianceToolInitialisationRequest;

public class MatchResponseAcceptanceTest {

    private static final String EXPECTED_PID = "verify-service-provider-pid";
    private static String COMPLIANCE_TOOL_HOST = "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk";

    @ClassRule
    public static DropwizardAppRule<VerifyServiceProviderConfiguration> application = new DropwizardAppRule<>(
        VerifyServiceProviderApplication.class,
        resourceFilePath("verify-service-provider-acceptance-test.yml"),
        ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
        ConfigOverride.config("hubSsoLocation", String.format("%s/%s", COMPLIANCE_TOOL_HOST, "SAML2/SSO")),
        ConfigOverride.config("samlPrimaryEncryptionKey", TEST_RP_PRIVATE_ENCRYPTION_KEY)
    );

    private static Client client = application.client();
    private String successMatchResponse;

    @Before
    public void setupComplianceTool() throws Exception {
        Entity initialisationRequest = aComplianceToolInitialisationRequest()
            .withEncryptionCertificate(TEST_RP_PUBLIC_ENCRYPTION_CERT)
            .withExpectedPid(EXPECTED_PID)
            .build();

        Response complianceToolResponse = client
            .target(URI.create(String.format("%s/%s", COMPLIANCE_TOOL_HOST, "service-test-data")))
            .request()
            .buildPost(initialisationRequest)
            .invoke();

        assertThat(complianceToolResponse.getStatus()).isEqualTo(OK.getStatusCode());

        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", application.getLocalPort())))
            .request()
            .buildPost(json(new RequestGenerationBody(LevelOfAssurance.LEVEL_2)))
            .invoke();

        RequestResponseBody authnSaml = authnResponse.readEntity(RequestResponseBody.class);

        Response complianceToolSsoResponse = client
            .target(authnSaml.getSsoLocation())
            .request()
            .buildPost(form(new MultivaluedHashMap<>(ImmutableMap.of("SAMLRequest", authnSaml.getSamlRequest()))))
            .invoke();

        JSONObject complianceToolResponseBody = new JSONObject(complianceToolSsoResponse.readEntity(String.class));
        String responseGeneratorLocation = complianceToolResponseBody.getString("responseGeneratorLocation");

        Response complianceToolScenariosResponse = client.target(responseGeneratorLocation)
            .request()
            .buildGet()
            .invoke();

        JSONObject complianceToolScenarios = new JSONObject(complianceToolScenariosResponse.readEntity(String.class));
        JSONArray testCases = complianceToolScenarios.getJSONArray("testCases");
        JSONObject successCase = testCases.getJSONObject(0);

        String successCaseURI = successCase.getString("executeUri");

        Response response = client.target(successCaseURI)
            .request()
            .buildGet()
            .invoke();
        String responseHtmlString = response.readEntity(String.class);
        successMatchResponse = extractSamlResponse(responseHtmlString);

        assertThat(successMatchResponse).isNotEmpty();
    }

    private String extractSamlResponse(String complianceToolResponseBody) {
        Elements responseElements = Jsoup.parse(complianceToolResponseBody)
            .getElementsByAttributeValue("name", "SAMLResponse");
        return responseElements.get(0).val();
    }

    @Test
    public void shouldHandleASuccessMatchResponse() {
        Map<String, String> translateResponseRequest = ImmutableMap.of(
            "samlResponse", successMatchResponse,
            "requestId", "string-to-be-fixed"
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequest))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(TranslatedResponseBody.class)).isEqualTo(new TranslatedResponseBody(
            "MATCH",
            EXPECTED_PID,
            LevelOfAssurance.LEVEL_2,
            null)
        );
    }
}
