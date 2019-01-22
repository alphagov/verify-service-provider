package uk.gov.ida.verifyserviceprovider.compliance;

import com.amazonaws.util.Base64;
import com.google.common.collect.ImmutableMap;
import uk.gov.ida.verifyserviceprovider.compliance.domain.MatchingDataset;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

public class ComplianceToolClient {

    private static final String HOST = "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk";

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

    private Response initialiseV2With(Entity initialisationRequest) {
        Response complianceToolResponse = client
                .target(URI.create(HOST + "/relying-party-service-test-run"))
                .request()
                .buildPost(initialisationRequest)
                .invoke();

        return complianceToolResponse;
    }

    private Response initialize(MatchingDataset matchingDataset) throws CertificateEncodingException {
        String encodedSigningCertificate = Base64.encodeAsString(signingCertificate.getEncoded());
        String encodedEncryptionCertificate = Base64.encodeAsString(encryptionCertificate.getEncoded());
        ImmutableMap.Builder<Object, Object> builder = ImmutableMap.builder();

        builder.put("serviceEntityId", serviceEntityId);
        builder.put("assertionConsumerServiceUrl", url);
        builder.put("signingCertificate", encodedSigningCertificate);
        builder.put("encryptionCertificate", encodedEncryptionCertificate);
        builder.put("matchingDatasetJson", matchingDataset);
        builder.put("isMatching", false);
        return initialiseV2With(Entity.json(builder.build()));
    }

    public Response initializeComplianceTool(MatchingDataset matchingDataset) throws CertificateEncodingException {
        Response initialize = initialize(matchingDataset);

        if(initialize.getStatus() != 200) {
           throw new RuntimeException(String.format("Compliance Tool Initialization Failure: %s %s", initialize.getStatus(), initialize.readEntity(String.class)));
        }
        return initialize;
    }
}
