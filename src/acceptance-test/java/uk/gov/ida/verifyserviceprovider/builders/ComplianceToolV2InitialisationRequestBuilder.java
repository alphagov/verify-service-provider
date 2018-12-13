package uk.gov.ida.verifyserviceprovider.builders;

import uk.gov.ida.verifyserviceprovider.domain.MatchingAddressV2;
import uk.gov.ida.verifyserviceprovider.domain.MatchingAttributeV2;
import uk.gov.ida.verifyserviceprovider.domain.MatchingDatasetV2;

import javax.ws.rs.client.Entity;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

public class ComplianceToolV2InitialisationRequestBuilder {

    private String serviceEntityId = "http://verify-service-provider";
    private String assertionConsumerServiceUrl = "http://verify-service-provider/response";
    private String signingCertificate = TEST_RP_PUBLIC_SIGNING_CERT;
    private String encryptionCertificate = TEST_RP_PUBLIC_ENCRYPTION_CERT;
    private MatchingDatasetV2 matchingDataset = new MatchingDatasetV2(
            new MatchingAttributeV2("Bob", true, LocalDateTime.now().minusDays(30), LocalDateTime.now()),
            null,
            singletonList(new MatchingAttributeV2("Smith", true, LocalDateTime.now().minusDays(30), LocalDateTime.now())),
            new MatchingAttributeV2("NOT_SPECIFIED", true, LocalDateTime.now().minusDays(30), LocalDateTime.now()),
            null,
            singletonList(new MatchingAddressV2(true, LocalDateTime.now().minusDays(30), LocalDateTime.now(), "E1 8QS", Arrays.asList("The White Chapel Building" ,"10 Whitechapel High Street"), null, null)),
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

    public ComplianceToolV2InitialisationRequestBuilder withExpectedPid(String expectedPid) {
        this.matchingDataset.setPersisentId(expectedPid);
        return this;
    }

    /**
     * Note: this will override the expectedPid.
     */
    public ComplianceToolV2InitialisationRequestBuilder withMatchingDataSet(MatchingDatasetV2 matchingDataset) {
        this.matchingDataset = matchingDataset;
        return this;
    }
}
