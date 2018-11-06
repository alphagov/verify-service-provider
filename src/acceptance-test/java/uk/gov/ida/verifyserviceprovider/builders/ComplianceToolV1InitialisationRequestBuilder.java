package uk.gov.ida.verifyserviceprovider.builders;

import common.uk.gov.ida.verifyserviceprovider.servers.MockMsaServer;

import javax.ws.rs.client.Entity;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.emptyList;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;

public class ComplianceToolV1InitialisationRequestBuilder {

    private String serviceEntityId = "http://verify-service-provider";
    private String assertionConsumerServiceUrl = "http://verify-service-provider/response";
    private String signingCertificate = TEST_RP_PUBLIC_SIGNING_CERT;
    private String encryptionCertificate = TEST_RP_PUBLIC_ENCRYPTION_CERT;
    private String expectedPID = "default-expected-pid";
    private String matchingServiceEntityId = MockMsaServer.MSA_ENTITY_ID;
    private String matchingServiceSigningPrivateKey = TEST_RP_MS_PRIVATE_SIGNING_KEY;
    private List<String> userAccountCreationAttributes = emptyList();

    public static ComplianceToolV1InitialisationRequestBuilder aComplianceToolV1InitialisationRequest() {
        return new ComplianceToolV1InitialisationRequestBuilder();
    }

    public Entity build() {
        HashMap<String, Object> map = new HashMap<>();

        map.put("serviceEntityId", serviceEntityId);
        map.put("assertionConsumerServiceUrl", assertionConsumerServiceUrl);
        map.put("signingCertificate", signingCertificate);
        map.put("encryptionCertificate", encryptionCertificate);
        map.put("expectedPID", expectedPID);
        map.put("matchingServiceEntityId", matchingServiceEntityId);
        map.put("matchingServiceSigningPrivateKey", matchingServiceSigningPrivateKey);
        map.put("userAccountCreationAttributes", userAccountCreationAttributes);
        map.put("useSimpleProfile", false);

        return Entity.json(map);
    }

    public ComplianceToolV1InitialisationRequestBuilder withEntityId(String entityId) {
        this.serviceEntityId = entityId;
        return this;
    }

    public ComplianceToolV1InitialisationRequestBuilder withEncryptionCertificate(String encryptionCertificate) {
        this.encryptionCertificate = encryptionCertificate;
        return this;
    }

    public ComplianceToolV1InitialisationRequestBuilder withExpectedPid(String expectedPid) {
        this.expectedPID = expectedPid;
        return this;
    }

    public ComplianceToolV1InitialisationRequestBuilder withUserAccountCreationAttributes(List<String> userAccountCreationAttributes) {
        this.userAccountCreationAttributes = userAccountCreationAttributes;
        return this;
    }

    public ComplianceToolV1InitialisationRequestBuilder withMatchingServiceSigningPrivateKey(String privateKey) {
        this.matchingServiceSigningPrivateKey = privateKey;
        return this;
    }

    public ComplianceToolV1InitialisationRequestBuilder withMatchingServiceEntityId(String matchingServiceEntityId) {
        this.matchingServiceEntityId = matchingServiceEntityId;
        return this;
    }
}
