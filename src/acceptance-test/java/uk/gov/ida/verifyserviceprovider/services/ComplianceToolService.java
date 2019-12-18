package uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDataset;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.verifyserviceprovider.builders.ComplianceToolV1InitialisationRequestBuilder.aComplianceToolV1InitialisationRequest;
import static uk.gov.ida.verifyserviceprovider.builders.ComplianceToolV2InitialisationRequestBuilder.aComplianceToolV2InitialisationRequest;

public class ComplianceToolService {

    private static final String HOST = "https://compliance-tool-integration.cloudapps.digital";
    public static final String SSO_LOCATION = HOST + "/SAML2/SSO";

    public static final int BASIC_SUCCESSFUL_MATCH_WITH_LOA2_ID = 1;
    public static final int BASIC_NO_MATCH_ID = 2;
    public static final int NO_AUTHENTICATION_CONTEXT_ID = 3;
    public static final int AUTHENTICATION_FAILED_ID = 4;
    public static final int REQUESTER_ERROR_ID = 5;
    public static final int ACCOUNT_CREATION_LOA2_ID = 6;
    public static final int BASIC_SUCCESSFUL_MATCH_WITH_LOA1_ID = 7;
    public static final int ACCOUNT_CREATION_LOA1_ID = 8;
    public static final int BASIC_SUCCESSFUL_MATCH_WITH_ASSERTIONS_SIGNED_BY_HUB_ID = 9;
    public static final int VERIFIED_USER_ON_SERVICE_WITH_NON_MATCH_SETTING_ID = 10;
    public static final int NO_AUTHENTICATION_CONTEXT_WITH_NON_MATCH_SETTING_ID = 11;
    public static final int AUTHENTICATION_FAILED_WITH_NON_MATCH_SETTING_ID = 13;
    public static final int FRAUDULENT_MATCH_RESPONSE_WITH_NON_MATCH_SETTING_ID = 14;

    private final Client client;

    public ComplianceToolService(Client client) {
        this.client = client;
    }

    public void initialiseWith(Entity initialisationRequest) {
        Response complianceToolResponse = client
            .target(URI.create(HOST + "/service-test-data"))
            .request()
            .buildPost(initialisationRequest)
            .invoke();

        assertThat(complianceToolResponse.getStatus()).isEqualTo(OK.getStatusCode());
    }

    public void initialiseV2With(Entity initialisationRequest) {
        Response complianceToolResponse = client
                .target(URI.create(HOST + "/relying-party-service-test-run"))
                .request()
                .buildPost(initialisationRequest)
                .invoke();

        assertThat(complianceToolResponse.getStatus()).isEqualTo(OK.getStatusCode());
    }

    public void initialiseWithDefaults() {
        initialiseWithPid("default-expected-pid");
    }

    public void initialiseWithDefaultsForV2() {
        initialiseV2With(
                aComplianceToolV2InitialisationRequest()
                .build()
        );
    }

    public void initialiseWithPid(String pid) {
        initialiseWith(
            aComplianceToolV1InitialisationRequest()
                .withExpectedPid(pid)
                .build()
        );
    }

    public void initialiseWithMatchingDatasetForV2(MatchingDataset matchingDataset) {
        initialiseV2With(
            aComplianceToolV2InitialisationRequest()
                .withMatchingDataSet(matchingDataset)
                .build()
        );
    }

    public void initialiseWithEntityIdAndPid(String entityId, String pid) {
        initialiseWith(
            aComplianceToolV1InitialisationRequest()
                .withEntityId(entityId)
                .withExpectedPid(pid)
                .build()
        );
    }

    public String createResponseFor(String samlRequest, int testCaseId) {
        return getExtractedSamlResponse(getResponseUrlById(testCaseId, samlRequest));
    }

    private String getExtractedSamlResponse(String endpoint) {
        Response response = client.target(endpoint)
            .request()
            .buildGet()
            .invoke();

        String samlResponse = extractSamlResponse(response.readEntity(String.class));

        assertThat(samlResponse).isNotEmpty();

        return samlResponse;
    }

    private String getResponseUrlById(int testCaseId, String samlRequest) {
        Response complianceToolSsoResponse = client
            .target(SSO_LOCATION)
            .request()
            .buildPost(form(new MultivaluedHashMap<>(ImmutableMap.of("SAMLRequest", samlRequest))))
            .invoke();

        JSONObject complianceToolResponseBody = new JSONObject(complianceToolSsoResponse.readEntity(String.class));

        Response complianceToolScenariosResponse = client
            .target(complianceToolResponseBody.getString("responseGeneratorLocation"))
            .request()
            .buildGet()
            .invoke();

        JSONObject complianceToolScenarios = new JSONObject(complianceToolScenariosResponse.readEntity(String.class));
        for (Object object : complianceToolScenarios.getJSONArray("testCases")) {
            JSONObject jsonObject = (JSONObject) object;
            int id = jsonObject.getInt("id");
            if (id == testCaseId) {
                return jsonObject.getString("executeUri");
            }
        }

        throw new RuntimeException("Couldn't find a test case with id + " + testCaseId);
    }

    private String extractSamlResponse(String complianceToolResponseBody) {
        Elements responseElements = Jsoup.parse(complianceToolResponseBody)
            .getElementsByAttributeValue("name", "SAMLResponse");
        return responseElements.get(0).val();
    }

}
