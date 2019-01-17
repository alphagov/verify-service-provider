package uk.gov.ida.verifyserviceprovider.compliance;

import uk.gov.ida.verifyserviceprovider.compliance.domain.MatchingAddress;
import uk.gov.ida.verifyserviceprovider.compliance.domain.MatchingAttribute;
import uk.gov.ida.verifyserviceprovider.compliance.domain.MatchingDataset;

import javax.ws.rs.client.Entity;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

public class ComplianceToolV2InitialisationRequestBuilder {

    public ComplianceToolV2InitialisationRequestBuilder withServiceEntityId(String serviceEntityId) {
        this.serviceEntityId = serviceEntityId;
        return this;
    }

    public ComplianceToolV2InitialisationRequestBuilder withAssertionConsumerServiceUrl(String assertionConsumerServiceUrl) {
        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        return this;
    }

    public ComplianceToolV2InitialisationRequestBuilder withSigningCertificate(String signingCertificate) {
        this.signingCertificate = signingCertificate;
        return this;
    }

    public ComplianceToolV2InitialisationRequestBuilder withEncryptionCertificate(String encryptionCertificate) {
        this.encryptionCertificate = encryptionCertificate;
        return this;
    }

    private String serviceEntityId = "http://verify-service-provider";
    private String assertionConsumerServiceUrl = "http://verify-service-provider/response";
    private String signingCertificate = TEST_RP_PUBLIC_SIGNING_CERT;
    private String encryptionCertificate = TEST_RP_PUBLIC_ENCRYPTION_CERT;
    private MatchingDataset matchingDataset = new MatchingDataset(
            new MatchingAttribute("Bob", true, LocalDateTime.now().minusDays(30), LocalDateTime.now()),
            null,
            singletonList(new MatchingAttribute("Smith", true, LocalDateTime.now().minusDays(30), LocalDateTime.now())),
            new MatchingAttribute("NOT_SPECIFIED", true, LocalDateTime.now().minusDays(30), LocalDateTime.now()),
            null,
            singletonList(new MatchingAddress(true, LocalDateTime.now().minusDays(30), LocalDateTime.now(), "E1 8QS", Arrays.asList("The White Chapel Building" ,"10 Whitechapel High Street"), null, null)),
            UUID.randomUUID().toString()
    );

    public static ComplianceToolV2InitialisationRequestBuilder aComplianceToolV2InitialisationRequest() {
        return new ComplianceToolV2InitialisationRequestBuilder();
    }

    public Entity build() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("serviceEntityId", serviceEntityId);
        map.put("assertionConsumerServiceUrl", assertionConsumerServiceUrl);
        map.put("signingCertificate", signingCertificate);
        map.put("encryptionCertificate", encryptionCertificate);
        map.put("matchingDatasetJson", matchingDataset);

        return Entity.json(map);
    }

    /**
     * Note: this will override the expectedPid.
     */
    public ComplianceToolV2InitialisationRequestBuilder withMatchingDataSet(MatchingDataset matchingDataset) {
        this.matchingDataset = matchingDataset;
        return this;
    }
}
