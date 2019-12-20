package uk.gov.ida.verifyserviceprovider.compliance;

import com.google.common.collect.ImmutableMap;
import uk.gov.ida.verifyserviceprovider.compliance.dto.MatchingDataset;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class ComplianceToolClient {

    private static final String HOST = "https://compliance-tool-integration.cloudapps.digital";

    private final Client client;
    private final String url;
    private final String serviceEntityId;
    private final X509Certificate signingCertificate;
    private final X509Certificate encryptionCertificate;

    public ComplianceToolClient(Client client, String url, String serviceEntityId, X509Certificate signingCertificate, X509Certificate encryptionCertificate) {
        this.client = client;
        this.url = url;
        this.serviceEntityId = serviceEntityId;
        this.signingCertificate = signingCertificate;
        this.encryptionCertificate = encryptionCertificate;
    }

    private Response makeRequest(Entity initialisationRequestBody) {
        Response complianceToolResponse = client
                .target(URI.create(HOST + "/relying-party-service-test-run"))
                .request()
                .buildPost(initialisationRequestBody)
                .invoke();

        return complianceToolResponse;
    }

    private Entity buildRequestBody(MatchingDataset matchingDataset) throws CertificateEncodingException {
        String encodedSigningCertificate = Base64.getEncoder().encodeToString(signingCertificate.getEncoded());
        String encodedEncryptionCertificate = Base64.getEncoder().encodeToString(encryptionCertificate.getEncoded());
        ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();
        builder.put("serviceEntityId", serviceEntityId);
        builder.put("assertionConsumerServiceUrl", url);
        builder.put("signingCertificate", encodedSigningCertificate);
        builder.put("encryptionCertificate", encodedEncryptionCertificate);
        builder.put("matchingDatasetJson", matchingDataset);
        builder.put("isMatching", false);
        return Entity.json(builder.build());
    }

    public Response initializeComplianceTool(MatchingDataset matchingDataset) throws CertificateEncodingException {
        Entity initialisationRequestBody = buildRequestBody(matchingDataset);
        Response response = makeRequest(initialisationRequestBody);
        if(response.getStatus() != 200) {
           throw new RuntimeException(String.format("Compliance Tool Initialization Failure: %s %s", response.getStatus(), response.readEntity(String.class)));
        }
        return response;
    }
}
