package uk.gov.ida.verifyserviceprovider.services;

import com.google.common.collect.ImmutableMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.client.Entity.form;
import static javax.ws.rs.core.Response.Status.OK;
import static org.assertj.core.api.Assertions.assertThat;

public class ComplianceToolService {

    public final static String HOST = "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk";
    public final static String SSO_LOCATION = HOST + "/SAML2/SSO";

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

    public String createSuccessMatchResponseFor(String samlRequest) {
        return getExtractedSamlResponse(getSuccessfulMatchResponseUrlFor(samlRequest));
    }

    public String createUserAccountCreationResponseFor(String samlRequest) {
        return getExtractedSamlResponse(getUserAccountCreationResponseUrlFor(samlRequest));
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

    private String getSuccessfulMatchResponseUrlFor(String samlRequest) {
        JSONObject successCase = getComplianceToolTestCases(samlRequest).getJSONObject(0);

        return successCase.getString("executeUri");
    }

    private String getUserAccountCreationResponseUrlFor(String samlRequest) {
        JSONObject userAccountCreationCase = getComplianceToolTestCases(samlRequest).getJSONObject(5);

        return userAccountCreationCase.getString("executeUri");
    }

    private JSONArray getComplianceToolTestCases(String samlRequest) {
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

        return new JSONObject(complianceToolScenariosResponse.readEntity(String.class)).getJSONArray("testCases");
    }

    private String extractSamlResponse(String complianceToolResponseBody) {
        Elements responseElements = Jsoup.parse(complianceToolResponseBody)
            .getElementsByAttributeValue("name", "SAMLResponse");
        return responseElements.get(0).val();
    }
}
