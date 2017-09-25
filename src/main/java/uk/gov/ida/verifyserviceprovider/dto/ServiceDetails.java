package uk.gov.ida.verifyserviceprovider.dto;

public class ServiceDetails {
    private final String entityId;
    private final String assertionConsumerServiceUri;

    public ServiceDetails(String entityId, String assertionConsumerServiceUri) {
        this.entityId = entityId;
        this.assertionConsumerServiceUri = assertionConsumerServiceUri;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getAssertionConsumerServiceUri() {
        return assertionConsumerServiceUri;
    }
}
