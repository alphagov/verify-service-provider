package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.dto.Scenario;
import uk.gov.ida.verifyserviceprovider.dto.TranslatedResponseBody;
import uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppRule;
import uk.gov.ida.verifyserviceprovider.services.ComplianceToolService;
import uk.gov.ida.verifyserviceprovider.services.GenerateRequestService;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Map;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;
import static uk.gov.ida.verifyserviceprovider.services.ComplianceToolService.REQUESTER_ERROR_ID;

@Ignore
public class ErrorResponseAcceptanceTest {

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
    public void shouldRespondWithSuccessWhenRequestError() {
        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(application.getLocalPort());
        Map<String, String> translateResponseRequestData = ImmutableMap.of(
            "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), REQUESTER_ERROR_ID),
            "requestId", requestResponseBody.getRequestId(),
            "levelOfAssurance", LEVEL_2.name()
        );

        Response response = client
            .target(String.format("http://localhost:%d/translate-response", application.getLocalPort()))
            .request()
            .buildPost(json(translateResponseRequestData))
            .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());
        assertThat(response.readEntity(TranslatedResponseBody.class).getScenario()).isEqualTo(Scenario.REQUEST_ERROR);
    }
}
