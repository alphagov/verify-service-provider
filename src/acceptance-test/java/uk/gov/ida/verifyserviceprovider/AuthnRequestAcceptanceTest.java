package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.configuration.VerifyServiceProviderConfiguration;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.URI;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.verifyserviceprovider.builders.ComplianceToolInitialisationRequestBuilder.aComplianceToolInitialisationRequest;

public class AuthnRequestAcceptanceTest {

    private static String COMPLIANCE_TOOL_HOST = "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk";

    @ClassRule
    public static DropwizardAppRule<VerifyServiceProviderConfiguration> application = new DropwizardAppRule<>(
        VerifyServiceProviderApplication.class,
        resourceFilePath("verify-service-provider-acceptance-test.yml"),
        ConfigOverride.config("samlSigningKey", TEST_RP_PRIVATE_SIGNING_KEY),
        ConfigOverride.config("hubSsoLocation", String.format("%s/%s", COMPLIANCE_TOOL_HOST, "SAML2/SSO"))
    );

    private static Client client = application.client();

    @Before
    public void setupComplianceTool() throws Exception {
        Entity initializationRequest = aComplianceToolInitialisationRequest().build();

        Response complianceToolResponse = client
            .target(URI.create(String.format("%s/%s", COMPLIANCE_TOOL_HOST, "service-test-data")))
            .request()
            .buildPost(initializationRequest)
            .invoke();

        assertThat(complianceToolResponse.getStatus()).isEqualTo(OK.getStatusCode());
    }

    @Test
    public void shouldGenerateValidAuthnRequest() throws Exception {
        Response authnResponse = client
            .target(URI.create(String.format("http://localhost:%d/generate-request", application.getLocalPort())))
            .request()
            .buildPost(Entity.json(new RequestGenerationBody(LevelOfAssurance.LEVEL_2)))
            .invoke();

        RequestResponseBody authnSaml = authnResponse.readEntity(RequestResponseBody.class);

        Response complianceToolResponse = client
            .target(authnSaml.getSsoLocation())
            .request()
            .buildPost(Entity.form(new MultivaluedHashMap<>(ImmutableMap.of("SAMLRequest", authnSaml.getSamlRequest()))))
            .invoke();

        JSONObject complianceToolResponseBody = new JSONObject(complianceToolResponse.readEntity(String.class));
        assertThat(complianceToolResponseBody.getJSONObject("status").get("message")).isEqualTo(null);
        assertThat(complianceToolResponseBody.getJSONObject("status").getString("status")).isEqualTo("PASSED");
    }
}
