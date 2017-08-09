package uk.gov.ida.verifyserviceprovider.services;

import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.client.Entity.json;
import static uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance.LEVEL_2;

public class GenerateRequestService {

    private final Client client;

    public GenerateRequestService(Client client) {
        this.client = client;
    }

    public RequestResponseBody generateAuthnRequest(int localPort) {
        return client
            .target(URI.create(String.format("http://localhost:%d/generate-request", localPort)))
            .request()
            .buildPost(json(new RequestGenerationBody(LEVEL_2)))
            .invoke()
            .readEntity(RequestResponseBody.class);
    }
}
