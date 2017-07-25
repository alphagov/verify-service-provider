package uk.gov.ida.verifyserviceprovider;

import java.util.List;

public class ComplianceToolServiceTestData {

    public final String serviceEntityId;
    public final String assertionConsumerServiceUrl;
    public final String signingCertificate;
    public final String encryptionCertificate;
    public final String expectedPID;
    public final String matchingServiceEntityId;
    public final String matchingServiceSigningPrivateKey;
    public final List<String> userAccountCreationAttributes;

    public ComplianceToolServiceTestData(String serviceEntityId,
                                         String assertionConsumerServiceUrl,
                                         String signingCertificate,
                                         String encryptionCertificate,
                                         String expectedPID,
                                         String matchingServiceEntityId,
                                         String matchingServiceSigningPrivateKey,
                                         List<String> userAccountCreationAttributes) {
        this.serviceEntityId = serviceEntityId;
        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        this.signingCertificate = signingCertificate;
        this.encryptionCertificate = encryptionCertificate;
        this.expectedPID = expectedPID;
        this.matchingServiceEntityId = matchingServiceEntityId;
        this.matchingServiceSigningPrivateKey = matchingServiceSigningPrivateKey;
        this.userAccountCreationAttributes = userAccountCreationAttributes;
    }
}
