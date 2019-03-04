package uk.gov.ida.verifyserviceprovider.services;

import uk.gov.ida.verifyserviceprovider.dto.RequestGenerationBody;
import uk.gov.ida.verifyserviceprovider.dto.RequestResponseBody;

import javax.ws.rs.client.Client;
import java.net.URI;

import static javax.ws.rs.client.Entity.json;

public class GenerateRequestService {

    private final Client client;

    public GenerateRequestService(Client client) {
        this.client = client;
    }

    public RequestResponseBody generateAuthnRequest(int localPort) {
        return client
            .target(URI.create(String.format("http://localhost:%d/generate-request", localPort)))
            .request()
            .buildPost(json(new RequestGenerationBody(null)))
            .invoke()
            .readEntity(RequestResponseBody.class);
    }

    public RequestResponseBody generateAuthnRequest(int localPort, String entityId) {
        return client
            .target(URI.create(String.format("http://localhost:%d/generate-request", localPort)))
            .request()
            .buildPost(json(new RequestGenerationBody(entityId)))
            .invoke()
            .readEntity(RequestResponseBody.class);
    }
}
