package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.dto.ErrorBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppRule;
import uk.gov.ida.verifyserviceprovider.services.ComplianceToolService;
import uk.gov.ida.verifyserviceprovider.services.GenerateRequestService;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Map;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

public class InvalidSamlAcceptanceTest {

    @ClassRule
    public static MockMsaServer msaServer = new MockMsaServer();

    @ClassRule
    public static VerifyServiceProviderAppRule application = new VerifyServiceProviderAppRule(msaServer);

    private static Client client = application.client();
    private static ComplianceToolService complianceTool = new ComplianceToolService(client);
    private static GenerateRequestService generateRequestService = new GenerateRequestService(client);

    @Before
    public void setUp() {
        complianceTool.initialiseWithDefaults();
    }

    @Test
    public void shouldRespondWithErrorWhenAssertionSignedByHub() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = ImmutableMap.of(
            "samlResponse", complianceTool.createIncorrectlySignedMatchResponseFor(requestResponseBody.getSamlRequest()),
            "requestId", requestResponseBody.getRequestId()
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        ErrorBody errorBody = response.readEntity(ErrorBody.class);

        assertThat(response.getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
        assertThat(errorBody.getReason()).isEqualTo("BAD_REQUEST");
        assertThat(errorBody.getMessage()).contains("SAML Validation Specification: Signature was not valid.");
    }
}
