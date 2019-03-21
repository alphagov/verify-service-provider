package uk.gov.ida.verifyserviceprovider;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;
import uk.gov.ida.verifyserviceprovider.rules.VerifyServiceProviderAppRule;
import uk.gov.ida.verifyserviceprovider.services.ComplianceToolService;
import uk.gov.ida.verifyserviceprovider.services.GenerateRequestService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.builders.VerifyServiceProviderAppRuleBuilder.aVerifyServiceProviderAppRule;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_1;
import static uk.gov.ida.verifyserviceprovider.dto.NonMatchingScenario.IDENTITY_VERIFIED;
import static uk.gov.ida.verifyserviceprovider.services.ComplianceToolService.VERIFIED_USER_ON_SERVICE_WITH_NON_MATCH_SETTING_ID;
import static uk.gov.ida.verifyserviceprovider.tracing.IstioHeaders.X_B3_FLAGS;
import static uk.gov.ida.verifyserviceprovider.tracing.IstioHeaders.X_B3_PARENTSPANID;
import static uk.gov.ida.verifyserviceprovider.tracing.IstioHeaders.X_B3_SAMPLED;
import static uk.gov.ida.verifyserviceprovider.tracing.IstioHeaders.X_B3_SPANID;
import static uk.gov.ida.verifyserviceprovider.tracing.IstioHeaders.X_B3_TRACEID;
import static uk.gov.ida.verifyserviceprovider.tracing.IstioHeaders.X_OT_SPAN_CONTEXT;
import static uk.gov.ida.verifyserviceprovider.tracing.IstioHeaders.X_REQUEST_ID;

public class IstioHeaderAcceptanceTest {

    private final String SOME_RANDOM_HEADER = "some-random-header";

    @ClassRule
    public static VerifyServiceProviderAppRule applicationWithEidasDisabled = aVerifyServiceProviderAppRule()
            .withEidasEnabledFlag(false)
            .build();

    @Test
    public void shouldProcessIdpResponseCorrectlyWhenEuropeanIdentityDisabled() {

        Client client = applicationWithEidasDisabled.client();
        ComplianceToolService complianceTool = new ComplianceToolService(client);
        GenerateRequestService generateRequestService = new GenerateRequestService(client);

        complianceTool.initialiseWithDefaultsForV2();

        RequestResponseBody requestResponseBody = generateRequestService.generateAuthnRequest(applicationWithEidasDisabled.getLocalPort());
        Map<String, String> translateResponseRequestData = ImmutableMap.of(
                "samlResponse", complianceTool.createResponseFor(requestResponseBody.getSamlRequest(), VERIFIED_USER_ON_SERVICE_WITH_NON_MATCH_SETTING_ID),
                "requestId", requestResponseBody.getRequestId(),
                "levelOfAssurance", LEVEL_1.name()
        );

        Response response = client
                .target(String.format("http://localhost:%d/translate-response", applicationWithEidasDisabled.getLocalPort()))
                .request()
                .headers(getTraceHeaders())
                .buildPost(json(translateResponseRequestData))
                .invoke();

        assertThat(response.getStatus()).isEqualTo(OK.getStatusCode());

        JSONObject jsonResponse = new JSONObject(response.readEntity(String.class));
        assertThat(jsonResponse.getString("scenario")).isEqualTo(IDENTITY_VERIFIED.name());
        assertThat(jsonResponse.getString("levelOfAssurance")).isEqualTo(LEVEL_1.name());

        assertRequiredTraceHeaders(response);
    }

    @Test
    public void shouldGenerateValidAuthnRequestUsingDefaultEntityId() throws Exception {

        Client client = applicationWithEidasDisabled.client();
        ComplianceToolService complianceTool = new ComplianceToolService(client);
        complianceTool.initialiseWithDefaultsForV2();

        Response authnResponse = client
                .target(URI.create(String.format("http://localhost:%d/generate-request", applicationWithEidasDisabled.getLocalPort())))
                .request()
                .headers(getTraceHeaders())
                .buildPost(Entity.json(new RequestGenerationBody(null)))
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

        assertRequiredTraceHeaders(authnResponse);
    }

    private MultivaluedMap<String, Object> getTraceHeaders() {
        MultivaluedMap<String, Object> traceHeaders =
                new MultivaluedHashMap<>();
        traceHeaders.add(X_REQUEST_ID, X_REQUEST_ID);
        traceHeaders.add(X_B3_TRACEID, X_B3_TRACEID);
        traceHeaders.add(X_B3_SPANID, X_B3_SPANID);
        traceHeaders.add(X_B3_PARENTSPANID, X_B3_PARENTSPANID);
        traceHeaders.add(X_B3_SAMPLED, X_B3_SAMPLED);
        traceHeaders.add(X_B3_FLAGS, X_B3_FLAGS);
        traceHeaders.add(X_OT_SPAN_CONTEXT, X_OT_SPAN_CONTEXT);
        traceHeaders.add(SOME_RANDOM_HEADER, SOME_RANDOM_HEADER);
        return traceHeaders;
    }

    private void assertRequiredTraceHeaders(Response response) {
        assertThat(response.getHeaders().getFirst(X_REQUEST_ID)).isEqualTo(X_REQUEST_ID);
        assertThat(response.getHeaders().getFirst(X_B3_TRACEID)).isEqualTo(X_B3_TRACEID);
        assertThat(response.getHeaders().getFirst(X_B3_SPANID)).isEqualTo(X_B3_SPANID);
        assertThat(response.getHeaders().getFirst(X_B3_PARENTSPANID)).isEqualTo(X_B3_PARENTSPANID);
        assertThat(response.getHeaders().getFirst(X_B3_SAMPLED)).isEqualTo(X_B3_SAMPLED);
        assertThat(response.getHeaders().getFirst(X_B3_FLAGS)).isEqualTo(X_B3_FLAGS);
        assertThat(response.getHeaders().getFirst(X_OT_SPAN_CONTEXT)).isEqualTo(X_OT_SPAN_CONTEXT);
        assertThat(response.getHeaders().containsKey(SOME_RANDOM_HEADER)).isFalse();
    }
}
